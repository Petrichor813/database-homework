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


def create_connection():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("数据库连接成功")
        return conn
    except mysql.connector.Error as err:
        print(f"数据库连接错误: {err}")
        sys.exit(1)


def get_certified_volunteers(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
) -> List[Dict]:
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
    cursor = conn.cursor(dictionary=True)
    try:
        cursor.execute(
            "SELECT id, title, type, start_time, end_time, status, points_per_hour, max_participants, create_time FROM activity WHERE id IN (2001, 2002, 2003, 2004)"
        )
        activities = cursor.fetchall()
        print(f"找到 {len(activities)} 个活动")
        return activities
    finally:
        cursor.close()


def generate_signup_records(
    volunteers: List[Dict], activities: List[Dict]
) -> List[Dict]:
    fake = Faker("zh_CN")
    signup_records: List[Dict] = []

    # 为每个活动设置不同的报名比例
    activity_ratios = {
        2001: 0.90,  # 儿童心理服务: 90%
        2002: 0.40,  # 非遗文化讲座: 40%
        2003: 0.60,  # 社区绿化维护: 60%
        2004: 0.80,  # 残疾人技能培训: 80%
    }

    for activity in tqdm(activities, desc="生成报名记录"):
        activity_id = activity["id"]
        start_time = activity["start_time"]
        end_time = activity["end_time"]
        create_time = activity["create_time"]
        max_participants = activity["max_participants"]

        # 根据比例计算报名人数
        ratio = activity_ratios.get(activity_id, 0.5)
        num_signups = int(max_participants * ratio)

        print(
            f"活动 {activity_id} ({activity['title']}): 最大人数 {max_participants}, 报名人数 {num_signups}, 比例 {ratio*100:.0f}%"
        )

        # 随机选择志愿者（避免重复）
        selected_volunteers = random.sample(
            volunteers, min(num_signups, len(volunteers))
        )

        for volunteer in selected_volunteers:
            volunteer_id = volunteer["id"]

            # 报名时间在create_time和start_time之间
            signup_time = create_time + timedelta(
                minutes=random.randint(
                    0, int((start_time - create_time).total_seconds() / 60)
                )
            )

            # 志愿者填的开始和结束时间（与活动时间一致）
            volunteer_start_time = start_time
            volunteer_end_time = end_time

            # 状态统一为CONFIRMED
            status = "CONFIRMED"

            # 实际服务时长和积分都为0（活动还未开始）
            actual_hours = 0
            points = 0

            # 更新时间：报名时间后随机时间
            update_time = signup_time + timedelta(minutes=random.randint(5, 60))

            # 备注不填（管理员在活动后填写）
            note = None

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


def print_statistics(conn: PooledMySQLConnection | MySQLConnectionAbstract):
    cursor = conn.cursor()

    try:
        # 查询这4个活动的报名情况
        cursor.execute(
            """
            SELECT 
                a.id,
                a.title,
                a.max_participants,
                COUNT(sr.id) as current_participants,
                ROUND(COUNT(sr.id) * 100.0 / a.max_participants, 1) as ratio
            FROM activity a
            LEFT JOIN signup_record sr ON a.id = sr.activity_id
            WHERE a.id IN (2001, 2002, 2003, 2004)
            GROUP BY a.id, a.title, a.max_participants
            ORDER BY a.id
        """
        )

        print("\n活动报名统计:")
        print(f"{'ID':<6} {'活动名称':<20} {'最大人数':<8} {'当前报名':<8} {'比例':<8}")
        print("-" * 60)
        for row in cursor.fetchall():
            print(f"{row[0]:<6} {row[1]:<20} {row[2]:<8} {row[3]:<8} {row[4]:<8}%")

    except mysql.connector.Error as err:
        print(f"查询统计信息时出错: {err}")
    finally:
        cursor.close()


def main():
    print("为4个活动生成报名记录（简化版）")

    conn = create_connection()

    try:
        print("\n获取志愿者和活动数据...")
        volunteers = get_certified_volunteers(conn)
        activities = get_activities(conn)

        if not volunteers:
            print("错误: 没有找到已认证的志愿者")
            return

        if not activities:
            print("错误: 没有找到活动")
            return

        print("\n生成报名记录...")
        signup_records = generate_signup_records(volunteers, activities)

        print("\n插入报名记录...")
        signup_ids = insert_signup_records(conn, signup_records)

        if not signup_ids:
            print("错误: 插入报名记录失败")
            return

        print("\n统计信息:")
        print_statistics(conn)

    finally:
        conn.close()
        print("数据库连接已关闭")


if __name__ == "__main__":
    main()
