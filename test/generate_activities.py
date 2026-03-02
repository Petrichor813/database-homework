import sys
import argparse
import random
from datetime import datetime, timedelta
from typing import List, Dict
import mysql.connector
from faker import Faker
from tqdm import tqdm

from mysql.connector.abstracts import MySQLConnectionAbstract
from mysql.connector.pooling import PooledMySQLConnection

NUM_ACTIVITIES_PER_YEAR = 500
YEARS = [2022, 2023, 2024, 2025]

DB_CONFIG: Dict[str, str | int] = {
    "host": "localhost",
    "port": 3306,
    "user": "volunteer",
    "password": "volunteer1227",
    "database": "volunteer",
}

ACTIVITY_TYPES = [
    "COMMUNITY_SERVICE",
    "ENVIRONMENTAL_PROTECTION",
    "ELDERLY_CARE",
    "CHILDREN_TUTORING",
    "DISABILITIES_SUPPORT",
    "CULTURAL_EVENTS",
    "HEALTH_PROMOTION",
    "OTHER",
]

ACTIVITY_STATUSES = [
    "RECRUITING",
    "CONFIRMED",
    "ONGOING",
    "COMPLETED",
    "CANCELLED",
]

LOCATIONS = [
    "社区活动中心",
    "市图书馆",
    "老年服务中心",
    "儿童福利院",
    "残疾人康复中心",
    "文化广场",
    "医院",
    "学校",
    "公园",
    "敬老院",
    "社区医院",
    "体育馆",
    "科技馆",
    "博物馆",
    "青少年宫",
]

ACTIVITY_TITLES = {
    "COMMUNITY_SERVICE": [
        "社区清洁志愿活动",
        "社区垃圾分类宣传",
        "社区安全巡逻",
        "社区绿化维护",
        "社区便民服务",
    ],
    "ENVIRONMENTAL_PROTECTION": [
        "植树造林活动",
        "河道清理行动",
        "环保知识讲座",
        "垃圾分类指导",
        "节能减排宣传",
    ],
    "ELDERLY_CARE": [
        "敬老院探访活动",
        "老年人健康体检",
        "老年人智能手机培训",
        "老年人陪伴服务",
        "老年人文艺表演",
    ],
    "CHILDREN_TUTORING": [
        "儿童课后辅导",
        "儿童安全教育",
        "儿童兴趣培养",
        "儿童心理辅导",
        "儿童阅读推广",
    ],
    "DISABILITIES_SUPPORT": [
        "残疾人康复训练",
        "残疾人技能培训",
        "残疾人心理疏导",
        "残疾人生活照料",
        "残疾人就业指导",
    ],
    "CULTURAL_EVENTS": [
        "传统文化讲座",
        "文艺演出志愿服务",
        "文化节庆活动",
        "非遗文化传承",
        "文化交流活动",
    ],
    "HEALTH_PROMOTION": [
        "健康知识宣传",
        "免费义诊活动",
        "健康体检服务",
        "健康生活方式推广",
        "心理健康讲座",
    ],
    "OTHER": [
        "志愿服务培训",
        "志愿者交流活动",
        "公益募捐活动",
        "公益宣传推广",
        "志愿服务表彰",
    ],
}

ACTIVITY_DESCRIPTIONS = [
    "本次活动旨在为社会提供志愿服务，帮助有需要的人群。",
    "通过本次活动，我们希望能够提高社会公众的意识和参与度。",
    "这是一次有意义的公益活动，欢迎广大志愿者积极参与。",
    "本次活动将为大家提供一个展示自我、服务社会的平台。",
    "让我们一起行动起来，为社会贡献自己的一份力量。",
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


def round_to_10_minutes(dt: datetime) -> datetime:
    """将时间向上取整到10分钟"""
    minutes = dt.minute
    rounded_minutes = ((minutes + 9) // 10) * 10
    if rounded_minutes >= 60:
        return dt.replace(minute=0, second=0, microsecond=0) + timedelta(hours=1)
    return dt.replace(minute=rounded_minutes, second=0, microsecond=0)


def generate_activity_time_slots(year: int) -> List[Dict[str, datetime]]:
    """生成一年的活动时间段"""
    time_slots = []
    
    for month in range(1, 13):
        for day in range(1, 32):
            try:
                date = datetime(year, month, day)
            except ValueError:
                continue
            
            daily_slots = []
            
            for period_start, period_end in [(8, 12), (14, 17)]:
                period_start_time = date.replace(hour=period_start, minute=0, second=0, microsecond=0)
                period_end_time = date.replace(hour=period_end, minute=0, second=0, microsecond=0)
                
                current_time = period_start_time
                
                while current_time < period_end_time:
                    start_time = current_time
                    
                    duration_minutes = random.choice([60, 90, 120, 150, 180])
                    end_time_candidate = start_time + timedelta(minutes=duration_minutes)
                    
                    if end_time_candidate <= period_end_time:
                        end_time = end_time_candidate
                        daily_slots.append({"start": start_time, "end": end_time})
                        
                        next_start_offset = random.choice([0, 10, 20, 30])
                        current_time = end_time + timedelta(minutes=next_start_offset)
                    else:
                        break
            
            num_activities_today = random.randint(1, min(5, len(daily_slots)))
            time_slots.extend(daily_slots[:num_activities_today])
    
    return time_slots


def generate_activities(year: int, num_activities: int):
    """生成活动数据"""
    fake = Faker("zh_CN")
    activities: List[Dict] = []
    
    time_slots = generate_activity_time_slots(year)
    
    if len(time_slots) < num_activities:
        print(f"警告: {year}年可用的活动时间段不足{num_activities}个，只有{len(time_slots)}个")
        num_activities = len(time_slots)
    
    selected_slots = time_slots[:num_activities]
    
    for i, slot in enumerate(selected_slots):
        activity_type = random.choice(ACTIVITY_TYPES)
        title = random.choice(ACTIVITY_TITLES[activity_type])
        location = random.choice(LOCATIONS)
        description = random.choice(ACTIVITY_DESCRIPTIONS)
        
        start_time = slot["start"]
        end_time = slot["end"]
        
        duration_minutes = (end_time - start_time).total_seconds() / 60
        points_per_hour = round(random.uniform(1.0, 5.0), 1)
        
        max_participants = random.randint(5, 50)
        cur_participants = random.randint(0, max_participants)
        
        create_days_before = random.randint(7, 15)
        create_time = start_time - timedelta(days=create_days_before)
        
        current_time = datetime(2026, 3, 1)
        
        if end_time < current_time:
            status = "COMPLETED"
        else:
            status = "RECRUITING"
        
        activities.append(
            {
                "title": title,
                "description": description,
                "type": activity_type,
                "location": location,
                "start_time": start_time,
                "end_time": end_time,
                "status": status,
                "points_per_hour": points_per_hour,
                "max_participants": max_participants,
                "cur_participants": cur_participants,
                "create_time": create_time,
            }
        )
    
    return activities


def insert_activities(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    activities: List[Dict],
):
    """插入活动数据"""
    cursor = conn.cursor()
    
    print("警告: 即将清空现有活动数据...")
    confirm = input("确认继续? (y/n): ")
    if confirm.lower() != "y":
        print("操作已取消")
        return
    
    try:
        cursor.execute("DELETE FROM signup_record")
        cursor.execute("DELETE FROM activity")
        cursor.execute("ALTER TABLE signup_record AUTO_INCREMENT = 1")
        cursor.execute("ALTER TABLE activity AUTO_INCREMENT = 1")
        print("已清空活动表和报名记录表，并重置自增ID")
        
        insert_activity_query = """
        INSERT INTO activity (title, description, type, location, start_time, end_time, status, points_per_hour, max_participants, cur_participants, create_time)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        
        for activity in tqdm(activities, desc="插入活动数据"):
            cursor.execute(
                insert_activity_query,
                (
                    activity["title"],
                    activity["description"],
                    activity["type"],
                    activity["location"],
                    activity["start_time"],
                    activity["end_time"],
                    activity["status"],
                    activity["points_per_hour"],
                    activity["max_participants"],
                    activity["cur_participants"],
                    activity["create_time"],
                ),
            )
        
        conn.commit()
        print(f"成功插入 {len(activities)} 个活动")
        
        cursor.execute("SELECT type, COUNT(*) FROM activity GROUP BY type")
        type_counts = cursor.fetchall()
        print("活动类型分布:")
        for act_type, count in type_counts:
            print(f"  {act_type}: {count}")
        
        cursor.execute("SELECT status, COUNT(*) FROM activity GROUP BY status")
        status_counts = cursor.fetchall()
        print("活动状态分布:")
        for status, count in status_counts:
            print(f"  {status}: {count}")
        
    except mysql.connector.Error as err:
        print(f"插入数据时出错: {err}")
        conn.rollback()
    finally:
        cursor.close()


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description="生成志愿服务平台活动数据")
    parser.add_argument(
        "--year",
        type=int,
        choices=YEARS,
        help="要生成活动的年份，不指定则生成所有年份"
    )
    parser.add_argument(
        "--num-activities",
        type=int,
        default=NUM_ACTIVITIES_PER_YEAR,
        help="每年要生成的活动数量"
    )
    parser.add_argument(
        "--clear-only",
        action="store_true",
        help="仅清空活动表和报名记录表，不插入新数据"
    )
    args = parser.parse_args()
    
    print("志愿者系统活动数据生成器")
    
    conn = create_connection()
    
    try:
        if args.clear_only:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM signup_record")
            cursor.execute("DELETE FROM activity")
            cursor.execute("ALTER TABLE signup_record AUTO_INCREMENT = 1")
            cursor.execute("ALTER TABLE activity AUTO_INCREMENT = 1")
            conn.commit()
            print("已清空活动表和报名记录表，并重置自增ID")
            cursor.close()
        else:
            years_to_generate = [args.year] if args.year else YEARS
            all_activities = []
            
            for year in years_to_generate:
                print(f"\n生成 {year} 年的活动数据...")
                activities = generate_activities(year, args.num_activities)
                all_activities.extend(activities)
                print(f"已生成 {len(activities)} 个活动")
            
            insert_activities(conn, all_activities)
            
    finally:
        conn.close()
        print("数据库连接已关闭")


if __name__ == "__main__":
    main()
