import sys
import argparse
import random
import math
from datetime import datetime, timedelta
from typing import List, Dict, Tuple
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

SIGNUP_STATUSES = [
    "REVIEWING",
    "CONFIRMED",
    "PARTICIPATED",
    "CANCELLED",
    "REJECTED",
    "UNARRIVED",
]

POINT_CHANGE_TYPES = [
    "ACTIVITY_EARN",
    "EXCHANGE_USE",
    "ADMIN_ADJUST",
    "SYSTEM_BONUS",
]

RELATED_RECORD_TYPES = ["SIGNUP", "EXCHANGE"]

NOTES = [
    "表现优秀",
    "积极参与",
    "服务认真",
    "按时到达",
    "配合度高",
    "态度良好",
    "完成任务",
    "团队合作",
]

REASONS = [
    "参与志愿服务活动",
    "完成活动任务",
    "志愿服务时长奖励",
    "活动参与奖励",
    "志愿服务贡献",
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
    """获取所有已认证的志愿者"""
    cursor = conn.cursor(dictionary=True)
    try:
        cursor.execute(
            "SELECT id, user_id, name, phone FROM volunteer WHERE status = 'CERTIFIED' AND deleted = FALSE"
        )
        volunteers = cursor.fetchall()
        print(f"找到 {len(volunteers)} 个已认证志愿者")
        return volunteers
    finally:
        cursor.close()


def get_activities(conn: PooledMySQLConnection | MySQLConnectionAbstract) -> List[Dict]:
    """获取所有活动"""
    cursor = conn.cursor(dictionary=True)
    try:
        cursor.execute(
            "SELECT id, title, type, start_time, end_time, status, points_per_hour, max_participants, create_time FROM activity"
        )
        activities = cursor.fetchall()
        print(f"找到 {len(activities)} 个活动")
        return activities
    finally:
        cursor.close()


def round_service_hours(duration_minutes: float) -> int:
    """
    将服务时长（分钟）四舍五入到小时
    
    Args:
        duration_minutes: 服务时长（分钟）
    
    Returns:
        四舍五入后的小时数
    """
    hours = duration_minutes / 60
    rounded_hours = round(hours)
    return rounded_hours


def calculate_points(actual_hours: int, points_per_hour: float) -> float:
    """
    计算积分，向下取整
    
    Args:
        actual_hours: 实际服务时长（小时）
        points_per_hour: 每小时积分
    
    Returns:
        积分（向下取整）
    """
    total_points = actual_hours * points_per_hour
    return math.floor(total_points)


def generate_signup_records(
    volunteers: List[Dict], activities: List[Dict]
) -> List[Dict]:
    """生成报名记录"""
    fake = Faker("zh_CN")
    signup_records: List[Dict] = []

    for activity in tqdm(activities, desc="生成报名记录"):
        activity_id = activity["id"]
        activity_status = activity["status"]
        start_time = activity["start_time"]
        end_time = activity["end_time"]
        create_time = activity["create_time"]
        points_per_hour = activity["points_per_hour"]
        max_participants = activity["max_participants"]

        # 随机生成报名人数（0到max_participants之间）
        num_signups = random.randint(0, max_participants)

        # 随机选择志愿者（避免重复）
        selected_volunteers = random.sample(volunteers, min(num_signups, len(volunteers)))

        for volunteer in selected_volunteers:
            volunteer_id = volunteer["id"]

            # 报名时间在create_time和start_time之间
            signup_time = create_time + timedelta(
                minutes=random.randint(0, int((start_time - create_time).total_seconds() / 60))
            )

            # 志愿者填的开始和结束时间（与活动时间一致）
            volunteer_start_time = start_time
            volunteer_end_time = end_time

            # 所有活动都是已结束的
            # 大部分为PARTICIPATED，少量为UNARRIVED
            status_weights = [0.0, 0.0, 0.95, 0.0, 0.0, 0.05]
            status = random.choices(SIGNUP_STATUSES, weights=status_weights)[0]

            # 计算实际服务时长和积分
            actual_hours = 0
            points = 0

            if status == "PARTICIPATED":
                # 已参与的活动：实际时长接近活动时长（±30分钟偏差）
                activity_duration_minutes = (end_time - start_time).total_seconds() / 60
                deviation = random.randint(-30, 30)
                actual_duration_minutes = activity_duration_minutes + deviation
                actual_hours = round_service_hours(actual_duration_minutes)
                points = calculate_points(actual_hours, points_per_hour)
            elif status == "UNARRIVED":
                # 未到场：实际时长和积分都为0
                actual_hours = 0
                points = 0

            # 更新时间：活动结束时间后30分钟
            update_time = end_time + timedelta(minutes=30)

            # 备注（可选）
            note = random.choice(NOTES) if random.random() < 0.5 else None

            signup_record = {
                "volunteerId": volunteer_id,
                "activityId": activity_id,
                "status": status,
                "actualHours": actual_hours,
                "points": points,
                "signupTime": signup_time,
                "volunteerStartTime": volunteer_start_time,
                "volunteerEndTime": volunteer_end_time,
                "updateTime": update_time,
                "note": note,
            }

            signup_records.append(signup_record)

    return signup_records


def insert_signup_records(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    signup_records: List[Dict],
) -> List[int]:
    """插入报名记录"""
    cursor = conn.cursor()

    try:
        insert_query = """
        INSERT INTO signup_record (volunteer_id, activity_id, status, actual_hours, points, signup_time, volunteer_start_time, volunteer_end_time, update_time, note)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """

        signup_ids = []
        for record in tqdm(signup_records, desc="插入报名记录"):
            cursor.execute(
                insert_query,
                (
                    record["volunteerId"],
                    record["activityId"],
                    record["status"],
                    record["actualHours"],
                    record["points"],
                    record["signupTime"],
                    record["volunteerStartTime"],
                    record["volunteerEndTime"],
                    record["updateTime"],
                    record["note"],
                ),
            )
            signup_ids.append(cursor.lastrowid)

        conn.commit()
        print(f"成功插入 {len(signup_records)} 条报名记录")

        return signup_ids

    except mysql.connector.Error as err:
        print(f"插入报名记录时出错: {err}")
        conn.rollback()
        return []
    finally:
        cursor.close()


def generate_point_change_records(
    signup_records: List[Dict], signup_ids: List[int], activities: List[Dict]
) -> List[Dict]:
    """生成积分变动记录"""
    fake = Faker("zh_CN")
    point_change_records: List[Dict] = []

    # 创建活动ID到活动信息的映射
    activity_map = {activity["id"]: activity for activity in activities}

    # 为每个报名记录生成积分变动记录（仅PARTICIPATED状态）
    for i, signup_record in enumerate(signup_records):
        if signup_record["status"] == "PARTICIPATED" and signup_record["points"] is not None:
            signup_id = signup_ids[i]
            volunteer_id = signup_record["volunteerId"]
            activity_id = signup_record["activityId"]
            points = signup_record["points"]
            activity = activity_map[activity_id]
            end_time = activity["end_time"]

            # 变动时间在活动结束时间之后
            change_time = end_time + timedelta(
                minutes=random.randint(10, 60)
            )

            # 变动原因和备注
            reason = random.choice(REASONS)
            note = random.choice(NOTES)

            point_change_record = {
                "volunteerId": volunteer_id,
                "changePoints": points,
                "changeType": "ACTIVITY_EARN",
                "reason": reason,
                "relatedRecordId": signup_id,
                "relatedRecordType": "SIGNUP",
                "balanceAfter": None,  # 稍后计算
                "changeTime": change_time,
                "note": note,
            }

            point_change_records.append(point_change_record)

    return point_change_records


def calculate_balance_after(
    point_change_records: List[Dict],
) -> List[Dict]:
    """计算每个志愿者的积分余额"""
    # 按志愿者分组
    volunteer_records: Dict[int, List[Dict]] = {}
    for record in point_change_records:
        volunteer_id = record["volunteerId"]
        if volunteer_id not in volunteer_records:
            volunteer_records[volunteer_id] = []
        volunteer_records[volunteer_id].append(record)

    # 按时间排序并计算余额
    for volunteer_id, records in volunteer_records.items():
        # 按变动时间排序
        records.sort(key=lambda x: x["changeTime"])

        # 计算余额
        balance = 0.0
        for record in records:
            balance += record["changePoints"]
            record["balanceAfter"] = balance

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


def print_statistics(conn: PooledMySQLConnection | MySQLConnectionAbstract):
    """打印统计信息"""
    cursor = conn.cursor()

    try:
        # 报名记录总数
        cursor.execute("SELECT COUNT(*) FROM signup_record")
        total_signups = cursor.fetchone()[0]
        print(f"\n报名记录总数: {total_signups}")

        # 积分变动记录总数
        cursor.execute("SELECT COUNT(*) FROM point_change_record")
        total_point_changes = cursor.fetchone()[0]
        print(f"积分变动记录总数: {total_point_changes}")

        # 报名状态分布
        cursor.execute("SELECT status, COUNT(*) FROM signup_record GROUP BY status")
        status_counts = cursor.fetchall()
        print("\n报名状态分布:")
        for status, count in status_counts:
            print(f"  {status}: {count}")

        # 志愿者积分统计
        cursor.execute("""
            SELECT 
                AVG(balance_after) as avg_points,
                MAX(balance_after) as max_points,
                MIN(balance_after) as min_points,
                COUNT(DISTINCT volunteer_id) as volunteer_count
            FROM (
                SELECT volunteer_id, balance_after
                FROM point_change_record
                WHERE (volunteer_id, change_time) IN (
                    SELECT volunteer_id, MAX(change_time)
                    FROM point_change_record
                    GROUP BY volunteer_id
                )
            ) as latest_balances
        """)
        stats = cursor.fetchone()
        print(f"\n志愿者积分统计:")
        print(f"  平均积分: {stats[0]:.2f}")
        print(f"  最高积分: {stats[1]:.2f}")
        print(f"  最低积分: {stats[2]:.2f}")
        print(f"  有积分变动的志愿者数: {stats[3]}")

        # 积分变动类型分布
        cursor.execute("SELECT change_type, COUNT(*) FROM point_change_record GROUP BY change_type")
        type_counts = cursor.fetchall()
        print("\n积分变动类型分布:")
        for change_type, count in type_counts:
            print(f"  {change_type}: {count}")

    except mysql.connector.Error as err:
        print(f"查询统计信息时出错: {err}")
    finally:
        cursor.close()


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description="生成志愿服务平台报名记录和积分变动记录数据")
    parser.add_argument(
        "--clear-only",
        action="store_true",
        help="仅清空报名记录表和积分变动记录表，不插入新数据"
    )
    args = parser.parse_args()

    print("志愿者系统报名记录和积分变动记录生成器")

    conn = create_connection()

    try:
        if args.clear_only:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM point_change_record")
            cursor.execute("DELETE FROM signup_record")
            cursor.execute("ALTER TABLE point_change_record AUTO_INCREMENT = 1")
            cursor.execute("ALTER TABLE signup_record AUTO_INCREMENT = 1")
            conn.commit()
            print("已清空报名记录表和积分变动记录表，并重置自增ID")
            cursor.close()
        else:
            print("\n获取志愿者和活动数据...")
            volunteers = get_certified_volunteers(conn)
            activities = get_activities(conn)

            if not volunteers:
                print("错误: 没有找到已认证的志愿者，请先运行generate_users.py生成志愿者数据")
                return

            if not activities:
                print("错误: 没有找到活动，请先运行generate_activities.py生成活动数据")
                return

            print("\n警告: 即将清空现有报名记录和积分变动记录数据...")
            confirm = input("确认继续? (y/n): ")
            if confirm.lower() != "y":
                print("操作已取消")
                return

            cursor = conn.cursor()
            cursor.execute("DELETE FROM point_change_record")
            cursor.execute("DELETE FROM signup_record")
            cursor.execute("ALTER TABLE point_change_record AUTO_INCREMENT = 1")
            cursor.execute("ALTER TABLE signup_record AUTO_INCREMENT = 1")
            conn.commit()
            print("已清空报名记录表和积分变动记录表，并重置自增ID")
            cursor.close()

            print("\n生成报名记录...")
            signup_records = generate_signup_records(volunteers, activities)

            print("\n插入报名记录...")
            signup_ids = insert_signup_records(conn, signup_records)

            if not signup_ids:
                print("错误: 插入报名记录失败")
                return

            print("\n生成积分变动记录...")
            point_change_records = generate_point_change_records(
                signup_records, signup_ids, activities
            )

            print("\n计算积分余额...")
            point_change_records = calculate_balance_after(point_change_records)

            print("\n插入积分变动记录...")
            insert_point_change_records(conn, point_change_records)

            print("\n统计信息:")
            print_statistics(conn)

    finally:
        conn.close()
        print("数据库连接已关闭")


if __name__ == "__main__":
    main()
