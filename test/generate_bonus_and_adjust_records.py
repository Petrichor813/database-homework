import sys
import random
from datetime import datetime, timedelta
from typing import List, Dict
import mysql.connector
from faker import Faker
from tqdm import tqdm

from mysql.connector.abstracts import MySQLConnectionAbstract
from mysql.connector.pooling import PooledMySQLConnection

DB_CONFIG: Dict[str, str | int] = {
    "host": "localhost",
    "port": 3306,
    "user": "volunteer",
    "password": "volunteer1227",
    "database": "volunteer",
}

# 系统奖励原因
SYSTEM_BONUS_REASONS = [
    "年度优秀志愿者奖励",
    "特殊贡献奖励",
    "长期服务奖励",
    "节日福利奖励",
    "活动组织奖励",
    "团队协作奖励",
    "创新服务奖励",
    "社区贡献奖励",
]

# 管理员调整原因
ADMIN_ADJUST_REASONS = [
    "积分补偿",
    "系统错误修正",
    "手动调整",
    "活动积分修正",
    "数据错误修正",
    "特殊情况处理",
    "积分调整",
    "人工审核调整",
]

# 备注信息
NOTES = [
    "表现优秀",
    "积极参与",
    "服务认真",
    "按时到达",
    "配合度高",
    "态度良好",
    "完成任务",
    "团队合作",
    "长期贡献",
    "特殊贡献",
]


def create_connection():
    """创建数据库连接"""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("数据库连接成功")
        return conn
    except mysql.connector.Error as err:
        print(f"数据库连接错误: {err}")
        sys.exit(1)


def get_certified_volunteers(conn: PooledMySQLConnection | MySQLConnectionAbstract) -> List[Dict]:
    """获取所有已认证的志愿者及其当前积分"""
    cursor = conn.cursor(dictionary=True)
    try:
        cursor.execute(
            "SELECT id, user_id, name, phone, points FROM volunteer WHERE status = 'CERTIFIED' AND deleted = FALSE"
        )
        volunteers = cursor.fetchall()
        print(f"找到 {len(volunteers)} 个已认证志愿者")
        return volunteers
    finally:
        cursor.close()


def get_current_max_change_time(conn: PooledMySQLConnection | MySQLConnectionAbstract) -> datetime:
    """获取当前最新的积分变动时间"""
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT MAX(change_time) FROM point_change_record")
        result = cursor.fetchone()
        if result[0]:
            return result[0]
        else:
            return datetime(2022, 1, 1)
    finally:
        cursor.close()


def get_volunteer_current_points(conn: PooledMySQLConnection | MySQLConnectionAbstract, volunteer_id: int) -> float:
    """获取志愿者当前积分"""
    cursor = conn.cursor()
    try:
        cursor.execute(
            "SELECT COALESCE(SUM(change_points), 0) FROM point_change_record WHERE volunteer_id = %s",
            (volunteer_id,)
        )
        result = cursor.fetchone()
        return float(result[0])
    finally:
        cursor.close()


def generate_system_bonus_records(
    volunteers: List[Dict],
    start_time: datetime,
    num_records: int = 200
) -> List[Dict]:
    """生成系统奖励记录"""
    fake = Faker("zh_CN")
    records: List[Dict] = []

    # 为每个志愿者随机生成系统奖励记录
    for volunteer in volunteers:
        # 随机决定是否给予奖励（约60%的志愿者会获得奖励）
        if random.random() < 0.6:
            # 随机生成奖励次数（1-3次）
            num_bonuses = random.randint(1, 3)

            for _ in range(num_bonuses):
                volunteer_id = volunteer["id"]
                current_points = volunteer["points"]

                # 奖励积分范围：5-50分
                bonus_points = round(random.uniform(5, 50), 1)

                # 奖励原因
                reason = random.choice(SYSTEM_BONUS_REASONS)

                # 备注
                note = random.choice(NOTES)

                # 变动时间：在start_time之后随机分布
                days_offset = random.randint(0, 365)
                hours_offset = random.randint(0, 23)
                minutes_offset = random.randint(0, 59)
                change_time = start_time + timedelta(days=days_offset, hours=hours_offset, minutes=minutes_offset)

                record = {
                    "volunteerId": volunteer_id,
                    "changePoints": bonus_points,
                    "changeType": "SYSTEM_BONUS",
                    "reason": reason,
                    "relatedRecordId": None,
                    "relatedRecordType": None,
                    "balanceAfter": None,  # 稍后计算
                    "changeTime": change_time,
                    "note": note,
                }

                records.append(record)

    return records


def generate_admin_adjust_records(
    volunteers: List[Dict],
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    start_time: datetime,
    num_records: int = 150
) -> List[Dict]:
    """生成管理员调整记录"""
    fake = Faker("zh_CN")
    records: List[Dict] = []

    # 为部分志愿者生成管理员调整记录（约40%的志愿者）
    selected_volunteers = random.sample(volunteers, int(len(volunteers) * 0.4))

    for volunteer in selected_volunteers:
        volunteer_id = volunteer["id"]

        # 随机生成调整次数（1-2次）
        num_adjusts = random.randint(1, 2)

        for _ in range(num_adjusts):
            # 获取志愿者当前积分
            current_points = get_volunteer_current_points(conn, volunteer_id)

            # 随机决定是增加还是减少积分
            is_increase = random.random() < 0.7  # 70%概率增加积分

            if is_increase:
                # 增加积分：5-30分
                adjust_points = round(random.uniform(5, 30), 1)
            else:
                # 减少积分：需要确保积分不会变成负数
                max_deduct = current_points
                if max_deduct > 0:
                    # 可以扣除的最大积分不超过当前积分的50%
                    max_deduct = min(max_deduct, current_points * 0.5)
                    # 扣除积分范围：5-max_deduct分
                    if max_deduct >= 5:
                        adjust_points = -round(random.uniform(5, max_deduct), 1)
                    else:
                        # 积分太少，不进行扣除
                        continue
                else:
                    # 积分为0，不能扣除
                    continue

            # 调整原因
            reason = random.choice(ADMIN_ADJUST_REASONS)

            # 备注
            note = random.choice(NOTES)

            # 变动时间：在start_time之后随机分布
            days_offset = random.randint(0, 365)
            hours_offset = random.randint(0, 23)
            minutes_offset = random.randint(0, 59)
            change_time = start_time + timedelta(days=days_offset, hours=hours_offset, minutes=minutes_offset)

            record = {
                "volunteerId": volunteer_id,
                "changePoints": adjust_points,
                "changeType": "ADMIN_ADJUST",
                "reason": reason,
                "relatedRecordId": None,
                "relatedRecordType": None,
                "balanceAfter": None,  # 稍后计算
                "changeTime": change_time,
                "note": note,
            }

            records.append(record)

    return records


def calculate_balance_after(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    new_records: List[Dict],
) -> List[Dict]:
    """计算每个志愿者的积分余额"""
    # 按志愿者分组
    volunteer_records: Dict[int, List[Dict]] = {}
    for record in new_records:
        volunteer_id = record["volunteerId"]
        if volunteer_id not in volunteer_records:
            volunteer_records[volunteer_id] = []
        volunteer_records[volunteer_id].append(record)

    # 为每个志愿者计算余额
    for volunteer_id, records in volunteer_records.items():
        # 获取志愿者当前的积分余额
        current_balance = get_volunteer_current_points(conn, volunteer_id)

        # 按变动时间排序
        records.sort(key=lambda x: x["changeTime"])

        # 计算每次变动后的余额
        for record in records:
            current_balance += record["changePoints"]
            record["balanceAfter"] = current_balance

    # 合并所有记录
    all_records = []
    for records in volunteer_records.values():
        all_records.extend(records)

    return all_records


def insert_point_change_records(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    point_change_records: List[Dict],
):
    """插入积分变动记录"""
    cursor = conn.cursor()

    try:
        insert_query = """
        INSERT INTO point_change_record (volunteer_id, change_points, change_type, reason, related_record_id, related_record_type, balance_after, change_time, note)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        """

        for record in tqdm(point_change_records, desc="插入积分变动记录"):
            cursor.execute(
                insert_query,
                (
                    record["volunteerId"],
                    record["changePoints"],
                    record["changeType"],
                    record["reason"],
                    record["relatedRecordId"],
                    record["relatedRecordType"],
                    record["balanceAfter"],
                    record["changeTime"],
                    record["note"],
                ),
            )

        conn.commit()
        print(f"成功插入 {len(point_change_records)} 条积分变动记录")

    except mysql.connector.Error as err:
        print(f"插入积分变动记录时出错: {err}")
        conn.rollback()
    finally:
        cursor.close()


def update_volunteer_points(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
):
    """更新志愿者积分字段"""
    cursor = conn.cursor()

    try:
        # 计算每个志愿者的积分总和并更新
        update_query = """
        UPDATE volunteer v
        SET v.points = (
            SELECT COALESCE(SUM(pcr.change_points), 0)
            FROM point_change_record pcr
            WHERE pcr.volunteer_id = v.id
        )
        WHERE v.deleted = FALSE
        """

        cursor.execute(update_query)
        conn.commit()

        # 获取更新的志愿者数量
        cursor.execute("SELECT ROW_COUNT()")
        updated_count = cursor.fetchone()[0]
        print(f"成功更新 {updated_count} 个志愿者的积分")

    except mysql.connector.Error as err:
        print(f"更新志愿者积分时出错: {err}")
        conn.rollback()
    finally:
        cursor.close()


def print_statistics(conn: PooledMySQLConnection | MySQLConnectionAbstract):
    """打印统计信息"""
    cursor = conn.cursor()

    try:
        # 积分变动记录总数
        cursor.execute("SELECT COUNT(*) FROM point_change_record")
        total_point_changes = cursor.fetchone()[0]
        print(f"\n积分变动记录总数: {total_point_changes}")

        # 积分变动类型分布
        cursor.execute("SELECT change_type, COUNT(*) FROM point_change_record GROUP BY change_type")
        type_counts = cursor.fetchall()
        print("\n积分变动类型分布:")
        for change_type, count in type_counts:
            print(f"  {change_type}: {count}")

        # 志愿者积分统计
        cursor.execute("""
            SELECT 
                AVG(points) as avg_points,
                MAX(points) as max_points,
                MIN(points) as min_points,
                COUNT(*) as volunteer_count
            FROM volunteer
            WHERE deleted = FALSE
        """)
        stats = cursor.fetchone()
        print(f"\n志愿者积分统计:")
        print(f"  平均积分: {stats[0]:.2f}")
        print(f"  最高积分: {stats[1]:.2f}")
        print(f"  最低积分: {stats[2]:.2f}")
        print(f"  志愿者总数: {stats[3]}")

        # 系统奖励统计
        cursor.execute("""
            SELECT 
                COUNT(*) as count,
                AVG(change_points) as avg_points,
                SUM(change_points) as total_points
            FROM point_change_record
            WHERE change_type = 'SYSTEM_BONUS'
        """)
        system_bonus_stats = cursor.fetchone()
        print(f"\n系统奖励统计:")
        print(f"  记录数: {system_bonus_stats[0]}")
        print(f"  平均奖励积分: {system_bonus_stats[1]:.2f}")
        print(f"  总奖励积分: {system_bonus_stats[2]:.2f}")

        # 管理员调整统计
        cursor.execute("""
            SELECT 
                COUNT(*) as count,
                AVG(change_points) as avg_points,
                SUM(change_points) as total_points
            FROM point_change_record
            WHERE change_type = 'ADMIN_ADJUST'
        """)
        admin_adjust_stats = cursor.fetchone()
        print(f"\n管理员调整统计:")
        print(f"  记录数: {admin_adjust_stats[0]}")
        print(f"  平均调整积分: {admin_adjust_stats[1]:.2f}")
        print(f"  总调整积分: {admin_adjust_stats[2]:.2f}")

    except mysql.connector.Error as err:
        print(f"查询统计信息时出错: {err}")
    finally:
        cursor.close()


def main():
    """主函数"""
    import argparse

    parser = argparse.ArgumentParser(description="生成志愿服务平台系统奖励和管理员调整积分记录数据")
    parser.add_argument(
        "--system-bonus",
        type=int,
        default=200,
        help="要生成的系统奖励记录数量（默认200）"
    )
    parser.add_argument(
        "--admin-adjust",
        type=int,
        default=150,
        help="要生成的管理员调整记录数量（默认150）"
    )
    args = parser.parse_args()

    print("志愿者系统积分记录生成器（系统奖励和管理员调整）")

    conn = create_connection()

    try:
        print("\n获取志愿者数据...")
        volunteers = get_certified_volunteers(conn)

        if not volunteers:
            print("错误: 没有找到已认证的志愿者，请先运行generate_users.py生成志愿者数据")
            return

        print("\n获取当前最新的积分变动时间...")
        start_time = get_current_max_change_time(conn)
        print(f"起始时间: {start_time}")

        print("\n生成系统奖励记录...")
        system_bonus_records = generate_system_bonus_records(
            volunteers, start_time, args.system_bonus
        )
        print(f"生成了 {len(system_bonus_records)} 条系统奖励记录")

        print("\n生成管理员调整记录...")
        admin_adjust_records = generate_admin_adjust_records(
            volunteers, conn, start_time, args.admin_adjust
        )
        print(f"生成了 {len(admin_adjust_records)} 条管理员调整记录")

        # 合并所有记录
        all_records = system_bonus_records + admin_adjust_records

        if not all_records:
            print("警告: 没有生成任何积分记录")
            return

        print("\n计算积分余额...")
        all_records = calculate_balance_after(conn, all_records)

        print("\n插入积分变动记录...")
        insert_point_change_records(conn, all_records)

        print("\n更新志愿者积分字段...")
        update_volunteer_points(conn)

        print("\n统计信息:")
        print_statistics(conn)

    finally:
        conn.close()
        print("数据库连接已关闭")


if __name__ == "__main__":
    main()
