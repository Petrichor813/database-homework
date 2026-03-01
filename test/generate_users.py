import sys
import argparse
import random
from datetime import datetime
import mysql.connector
from faker import Faker
from tqdm import tqdm
import bcrypt

from typing import List, Dict
from mysql.connector.abstracts import MySQLConnectionAbstract
from mysql.connector.pooling import PooledMySQLConnection

NUM_USERS = 1000
ADMIN_RATIO = 0.01
VOLUNTEER_RATIO = 0.8
NORMAL_USER_RATIO = 0.19

DB_CONFIG: Dict[str, str | int] = {
    "host": "localhost",
    "port": 3306,
    "user": "volunteer",
    "password": "volunteer1227",
    "database": "volunteer",
}

# BCrypt加密配置，与Java端BCryptPasswordEncoder默认配置一致
BCRYPT_ROUNDS = 10  # Java端BCryptPasswordEncoder默认strength


def create_connection():
    """创建数据库连接"""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("数据库连接成功")
        return conn
    except mysql.connector.Error as err:
        print(f"数据库连接错误: {err}")
        sys.exit(1)


def encrypt_password(password: str) -> str:
    """使用BCrypt加密密码，与Java端BCryptPasswordEncoder保持一致"""
    # 将密码转换为字节
    password_bytes = password.encode("utf-8")
    # 生成盐值并加密
    salt = bcrypt.gensalt(rounds=BCRYPT_ROUNDS)
    hashed = bcrypt.hashpw(password_bytes, salt)
    # 返回字符串形式的哈希值
    return hashed.decode("utf-8")


def generate_users(num_users: int):
    """生成用户数据"""
    fake = Faker("zh_CN")
    users: List[Dict[str, str | bool]] = []

    num_admins = int(num_users * ADMIN_RATIO)
    num_volunteers = int(num_users * VOLUNTEER_RATIO)
    num_normal_users = num_users - num_admins - num_volunteers

    print(
        f"生成 {num_admins} 个管理员用户，{num_volunteers} 个志愿者用户，{num_normal_users} 个普通用户"
    )

    for i in range(num_admins):
        username = f"admin_{i+1:03d}"
        password = "admin123"
        phone = fake.phone_number()
        name = fake.name()
        role = "ADMIN"

        users.append(
            {
                "username": username,
                "password": encrypt_password(password),  # 加密密码
                "phone": phone,
                "name": name,
                "role": role,
                "is_volunteer": True,  # 管理员也是志愿者
            }
        )

    for i in range(num_volunteers):
        username = f"volunteer_{i+1:03d}"
        password = "volunteer123"
        phone = fake.phone_number()
        name = fake.name()
        role = "VOLUNTEER"

        users.append(
            {
                "username": username,
                "password": encrypt_password(password),  # 加密密码
                "phone": phone,
                "name": name,
                "role": role,
                "is_volunteer": True,  # 志愿者角色用户在志愿者表中有记录
            }
        )

    for i in range(num_normal_users):
        username = f"user_{i+1:03d}"
        password = "user123"
        phone = fake.phone_number()
        name = fake.name()
        role = "USER"

        # 只有部分普通用户申请成为志愿者（约50%）
        is_volunteer = random.random() < 0.5

        users.append(
            {
                "username": username,
                "password": encrypt_password(password),  # 加密密码
                "phone": phone,
                "name": name,
                "role": role,
                "is_volunteer": is_volunteer,
            }
        )

    random.shuffle(users)
    return users


def insert_users(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    users: List[Dict[str, str | bool]],
):
    """插入用户数据"""
    cursor = conn.cursor()

    # 清空现有用户数据
    print("警告: 即将清空现有用户数据...")
    confirm = input("确认继续? (y/n): ")
    if confirm.lower() != "y":
        print("操作已取消")
        return

    try:
        # 清空用户表和志愿者表
        cursor.execute("DELETE FROM volunteer")
        cursor.execute("DELETE FROM `user`")
        cursor.execute("ALTER TABLE volunteer AUTO_INCREMENT = 1")
        cursor.execute("ALTER TABLE `user` AUTO_INCREMENT = 1")
        print("已清空用户表和志愿者表，并重置自增ID")

        # 插入用户数据
        insert_user_query = """
        INSERT INTO `user` (username, password, phone, role, deleted, create_time)
        VALUES (%s, %s, %s, %s, %s, %s)
        """

        current_time = datetime.now()

        # 使用tqdm显示进度条
        user_ids = []
        for user in tqdm(users, desc="插入用户数据"):
            cursor.execute(
                insert_user_query,
                (
                    user["username"],
                    user["password"],
                    user["phone"],
                    user["role"],
                    False,  # deleted字段
                    current_time,
                ),
            )

            # 获取插入的用户ID
            user_id = cursor.lastrowid
            user_ids.append((user_id, user))

        # 插入志愿者数据
        insert_volunteer_query = """
        INSERT INTO volunteer (user_id, name, phone, status, deleted, create_time, apply_reason, review_note, review_time)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        """

        volunteer_count = 0
        for user_id, user in tqdm(user_ids, desc="插入志愿者数据"):
            if user["is_volunteer"]:
                # 随机设置志愿者状态
                status_options = ["REVIEWING", "CERTIFIED", "REJECTED", "SUSPENDED"]
                status_weights = [0.2, 0.6, 0.15, 0.05]  # 大部分已认证
                status = random.choices(status_options, weights=status_weights)[0]

                # 设置审核时间和备注
                review_time = None  # 默认为空，只有审核通过/拒绝/停用时才设置
                review_note = None

                if status == "CERTIFIED":
                    review_note = "审核通过"
                    review_time = current_time  # 审核通过时设置审核时间
                elif status == "REJECTED":
                    review_note = "审核不通过"
                    review_time = current_time  # 审核拒绝时设置审核时间
                elif status == "SUSPENDED":
                    review_note = "违规停用"
                    review_time = current_time  # 停用时设置审核时间

                # 设置申请原因
                apply_reason_options = [
                    "希望为社会做出贡献",
                    "丰富个人经历",
                    "帮助有需要的人",
                    "提升社会责任感",
                    "参与社区建设",
                ]
                apply_reason = random.choice(apply_reason_options)

                # 创建时间（申请时间）使用当前时间
                create_time = current_time

                cursor.execute(
                    insert_volunteer_query,
                    (
                        user_id,
                        user["name"],
                        user["phone"],
                        status,
                        False,  # deleted字段
                        create_time,  # 申请时间
                        apply_reason,
                        review_note,
                        review_time,  # 审核时间
                    ),
                )

                volunteer_count += 1

        conn.commit()
        print(f"成功插入 {len(users)} 个用户")
        print(f"成功插入 {volunteer_count} 个志愿者")

        # 计算实际申请成为志愿者的普通用户数量
        normal_users_volunteer = sum(
            1 for user in users if user["role"] == "USER" and user["is_volunteer"]
        )
        normal_users_not_volunteer = sum(
            1 for user in users if user["role"] == "USER" and not user["is_volunteer"]
        )

        print(
            f"其中: {normal_users_volunteer} 个普通用户申请成为志愿者, {normal_users_not_volunteer} 个普通用户未申请志愿者"
        )

        # 显示各角色用户数量
        cursor.execute("SELECT role, COUNT(*) FROM `user` GROUP BY role")
        role_counts = cursor.fetchall()
        print("各角色用户数量:")
        for role, count in role_counts:
            print(f"  {role}: {count}")

        # 显示志愿者状态分布
        cursor.execute("SELECT status, COUNT(*) FROM volunteer GROUP BY status")
        status_counts = cursor.fetchall()
        print("志愿者状态分布:")
        for status, count in status_counts:
            print(f"  {status}: {count}")

    except mysql.connector.Error as err:
        print(f"插入数据时出错: {err}")
        conn.rollback()
    finally:
        cursor.close()


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description="生成志愿服务平台用户数据")
    parser.add_argument(
        "--num-users", type=int, default=NUM_USERS, help="要生成的用户数量"
    )
    parser.add_argument(
        "--clear-only", action="store_true", help="仅清空用户表和志愿者表，不插入新数据"
    )
    args = parser.parse_args()

    # 使用参数中的用户数量
    num_users = args.num_users

    print("志愿者系统用户数据生成器")
    print(
        f"配置: 总用户数 {num_users}, 管理员 {ADMIN_RATIO*100}%, 志愿者 {VOLUNTEER_RATIO*100}%, 普通用户 {NORMAL_USER_RATIO*100}%"
    )

    # 创建数据库连接
    conn = create_connection()

    try:
        if args.clear_only:
            # 仅清空数据
            cursor = conn.cursor()
            cursor.execute("DELETE FROM volunteer")
            cursor.execute("DELETE FROM `user`")
            cursor.execute("ALTER TABLE volunteer AUTO_INCREMENT = 1")
            cursor.execute("ALTER TABLE `user` AUTO_INCREMENT = 1")
            conn.commit()
            print("已清空用户表和志愿者表，并重置自增ID")
            cursor.close()
        else:
            # 生成用户数据
            users = generate_users(num_users)

            # 插入数据
            insert_users(conn, users)

            print("\n默认密码:")
            print("  管理员: admin123")
            print("  志愿者: volunteer123")
            print("  普通用户: user123")

    finally:
        conn.close()
        print("数据库连接已关闭")


if __name__ == "__main__":
    main()
