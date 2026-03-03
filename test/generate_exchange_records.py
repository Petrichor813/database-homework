import sys
import argparse
import random
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

EXCHANGE_STATUSES = [
    "REVIEWING",
    "PROCESSING",
    "COMPLETED",
    "CANCELLED",
    "REJECTED",
]

NOTES = [
    "商品兑换",
    "积分兑换",
    "礼品兑换",
    "奖励兑换",
]

RECV_ADDRESSES = [
    "北京市朝阳区xxx街道xxx号",
    "上海市浦东新区xxx路xxx号",
    "广州市天河区xxx大道xxx号",
    "深圳市南山区xxx路xxx号",
    "杭州市西湖区xxx街xxx号",
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


def get_all_volunteers(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
) -> List[Dict]:
    """获取所有已认证志愿者"""
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


def get_products(conn: PooledMySQLConnection | MySQLConnectionAbstract) -> List[Dict]:
    """获取所有商品"""
    cursor = conn.cursor(dictionary=True)
    try:
        cursor.execute(
            "SELECT id, name, price, stock, category FROM product WHERE status = 'AVAILABLE' ORDER BY price ASC"
        )
        products = cursor.fetchall()
        print(f"找到 {len(products)} 个可用商品")
        return products
    finally:
        cursor.close()


def get_volunteer_points_at_time(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    volunteer_id: int,
    target_time: datetime,
) -> float:
    """获取志愿者在指定时间的积分余额"""
    cursor = conn.cursor()
    try:
        # 查询在目标时间之前的积分变动记录总和
        cursor.execute(
            """
            SELECT COALESCE(SUM(change_points), 0) as balance
            FROM point_change_record
            WHERE volunteer_id = %s AND change_time <= %s
            """,
            (volunteer_id, target_time),
        )
        result = cursor.fetchone()
        return result[0] if result else 0.0
    finally:
        cursor.close()


def get_exchange_dates() -> List[datetime]:
    """获取兑换日期列表（每年3月15日、6月15日、9月15日、12月15日，2022-2025年）"""
    exchange_dates = []
    for year in range(2022, 2026):
        for month in [3, 6, 9, 12]:
            day = 15
            # 随机时间（9:00-18:00之间）
            hour = random.randint(9, 18)
            minute = random.randint(0, 59)
            exchange_date = datetime(year, month, day, hour, minute)
            exchange_dates.append(exchange_date)

    # 按时间排序
    exchange_dates.sort()
    return exchange_dates


def generate_exchange_records(
    volunteers: List[Dict],
    products: List[Dict],
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
) -> List[Dict]:
    """生成兑换记录"""
    fake = Faker("zh_CN")
    exchange_records: List[Dict] = []

    # 初始化商品库存跟踪（使用商品ID作为键）
    product_stock = {p["id"]: p["stock"] for p in products}

    # 初始化志愿者积分跟踪（使用志愿者ID作为键）
    # 存储每个志愿者在当前批次中的积分变化总和
    volunteer_points_changes: Dict[int, float] = {}

    # 获取兑换日期
    exchange_dates = get_exchange_dates()
    print(f"\n生成 {len(exchange_dates)} 个兑换日的兑换记录")

    # 为每个兑换日生成兑换记录
    for exchange_date in tqdm(exchange_dates, desc="按兑换日生成记录"):
        year = exchange_date.year
        month = exchange_date.month

        print(f"\n处理 {year}年{month}月15日 兑换日...")

        # 随机选取20-30名志愿者
        num_volunteers = random.randint(20, 30)
        selected_volunteers = random.sample(
            volunteers, min(num_volunteers, len(volunteers))
        )

        print(f"  随机选取 {len(selected_volunteers)} 名志愿者")

        # 为每个志愿者生成兑换记录
        for volunteer in selected_volunteers:
            volunteer_id = volunteer["id"]

            # 获取该时间点的积分余额（从数据库查询）
            points_at_time = get_volunteer_points_at_time(
                conn, volunteer_id, exchange_date
            )

            # 减去当前批次中该志愿者的积分变化（确保不会重复扣减）
            if volunteer_id in volunteer_points_changes:
                points_at_time -= volunteer_points_changes[volunteer_id]

            # 如果积分不足50，跳过
            if points_at_time < 50:
                continue

            # 选择可兑换的商品（价格不超过当前积分且库存大于0）
            affordable_products = [
                p for p in products
                if p["price"] <= points_at_time and product_stock[p["id"]] > 0
            ]

            if not affordable_products:
                continue

            # 随机决定兑换次数（1-2次）
            num_exchanges = random.randint(1, 2)

            # 随机选择商品并兑换
            for _ in range(num_exchanges):
                # 重新获取有库存的商品列表
                affordable_products = [
                    p for p in products
                    if p["price"] <= points_at_time and product_stock[p["id"]] > 0
                ]

                if not affordable_products:
                    break

                # 随机选择商品
                product = random.choice(affordable_products)
                product_id = product["id"]
                product_price = product["price"]

                # 计算可兑换的数量（不超过库存和2件）
                max_number = min(
                    int(points_at_time // product_price),
                    product_stock[product_id],
                    2
                )

                if max_number < 1:
                    break

                number = random.randint(1, max_number)

                # 计算总积分
                total_points = product_price * number

                # 确定兑换状态（大部分为COMPLETED，少量为其他状态）
                # 2025年的兑换日可以有更多其他状态的记录
                if year == 2025:
                    status_weights = [0.05, 0.05, 0.85, 0.03, 0.02]
                else:
                    status_weights = [0.02, 0.02, 0.95, 0.01, 0.00]

                status = random.choices(EXCHANGE_STATUSES, weights=status_weights)[0]

                # 处理时间和备注
                process_time = None
                note = None

                if status == "COMPLETED":
                    # 处理时间在兑换日后的1-7天内
                    days_after = random.randint(1, 7)
                    hours_after = random.randint(9, 18)
                    minutes_after = random.randint(0, 59)
                    process_time = exchange_date + timedelta(days=days_after, hours=hours_after, minutes=minutes_after)
                    note = "兑换成功"
                elif status == "PROCESSING":
                    # 处理时间在兑换日后的1-24小时内
                    hours_after = random.randint(1, 24)
                    process_time = exchange_date + timedelta(hours=hours_after)
                    note = "处理中"
                elif status == "REJECTED":
                    # 处理时间在兑换日后的1-24小时内
                    hours_after = random.randint(1, 24)
                    process_time = exchange_date + timedelta(hours=hours_after)
                    note = "库存不足"
                elif status == "CANCELLED":
                    # 处理时间在兑换日后的1-24小时内
                    hours_after = random.randint(1, 24)
                    process_time = exchange_date + timedelta(hours=hours_after)
                    note = "用户取消"
                elif status == "REVIEWING":
                    note = "待处理"

                # 收货信息
                recv_info = f"{fake.name()}, {random.choice(RECV_ADDRESSES)}, {fake.phone_number()}"

                exchange_record = {
                    "volunteerId": volunteer_id,
                    "productId": product_id,
                    "number": number,
                    "totalPoints": total_points,
                    "status": status,
                    "orderTime": exchange_date,
                    "processTime": process_time,
                    "note": note,
                    "recvInfo": recv_info,
                }

                exchange_records.append(exchange_record)

                # 更新剩余积分（确保积分不会变成负数）
                points_at_time -= total_points

                # 更新商品库存（只有COMPLETED状态才真正扣减库存）
                if status == "COMPLETED":
                    product_stock[product_id] -= number
                    # 记录该志愿者的积分变化
                    if volunteer_id in volunteer_points_changes:
                        volunteer_points_changes[volunteer_id] += total_points
                    else:
                        volunteer_points_changes[volunteer_id] = total_points

                # 如果积分不足，停止兑换
                if points_at_time < 50:
                    break

    return exchange_records


def insert_exchange_records(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    exchange_records: List[Dict],
) -> List[int]:
    """插入兑换记录"""
    cursor = conn.cursor()

    try:
        insert_query = """
        INSERT INTO exchange_record (volunteer_id, product_id, number, total_points, status, order_time, process_time, note, recv_info)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        """

        exchange_ids = []
        for record in tqdm(exchange_records, desc="插入兑换记录"):
            cursor.execute(
                insert_query,
                (
                    record["volunteerId"],
                    record["productId"],
                    record["number"],
                    record["totalPoints"],
                    record["status"],
                    record["orderTime"],
                    record["processTime"],
                    record["note"],
                    record["recvInfo"],
                ),
            )
            exchange_ids.append(cursor.lastrowid)

        conn.commit()
        print(f"成功插入 {len(exchange_records)} 条兑换记录")

        return exchange_ids

    except mysql.connector.Error as err:
        print(f"插入兑换记录时出错: {err}")
        conn.rollback()
        return []
    finally:
        cursor.close()


def update_product_stock(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
    exchange_records: List[Dict],
):
    """更新商品库存"""
    cursor = conn.cursor()

    try:
        # 统计每个商品的兑换数量（仅COMPLETED状态）
        product_stock_changes = {}
        for record in exchange_records:
            if record["status"] == "COMPLETED":
                product_id = record["productId"]
                number = record["number"]
                if product_id in product_stock_changes:
                    product_stock_changes[product_id] += number
                else:
                    product_stock_changes[product_id] = number

        # 更新商品库存
        for product_id, number in product_stock_changes.items():
            cursor.execute(
                "UPDATE product SET stock = stock - %s WHERE id = %s AND stock >= %s",
                (number, product_id, number)
            )

        # 更新库存为0的商品状态为SOLD_OUT
        cursor.execute(
            "UPDATE product SET status = 'SOLD_OUT' WHERE stock = 0 AND status = 'AVAILABLE'"
        )

        conn.commit()
        print(f"成功更新 {len(product_stock_changes)} 个商品的库存")

    except mysql.connector.Error as err:
        print(f"更新商品库存时出错: {err}")
        conn.rollback()
    finally:
        cursor.close()


def restore_product_stock(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
):
    """还原商品库存到初始值"""
    cursor = conn.cursor()

    try:
        # 第一步：在删除兑换记录之前，查询并保存每个商品的初始库存
        # 初始库存 = 当前库存 + 已兑换数量
        cursor.execute(
            """
            SELECT p.id, p.stock, COALESCE(SUM(er.number), 0) as exchanged
            FROM product p
            LEFT JOIN exchange_record er ON p.id = er.product_id AND er.status = 'COMPLETED'
            GROUP BY p.id, p.stock
            """
        )
        products = cursor.fetchall()

        # 保存初始库存信息
        initial_stocks = {}
        for product_id, current_stock, exchanged in products:
            initial_stock = current_stock + exchanged
            initial_stocks[product_id] = initial_stock

        # 第二步：删除兑换记录
        cursor.execute("DELETE FROM exchange_record")
        conn.commit()

        # 第三步：使用保存的初始库存来还原库存，并恢复状态为AVAILABLE
        for product_id, initial_stock in initial_stocks.items():
            cursor.execute(
                "UPDATE product SET stock = %s, status = 'AVAILABLE' WHERE id = %s",
                (initial_stock, product_id)
            )

        conn.commit()
        print(f"成功还原 {len(initial_stocks)} 个商品的库存")

    except mysql.connector.Error as err:
        print(f"还原商品库存时出错: {err}")
        conn.rollback()
    finally:
        cursor.close()


def generate_exchange_point_change_records(
    exchange_records: List[Dict], exchange_ids: List[int]
) -> List[Dict]:
    """生成兑换积分变动记录"""
    fake = Faker("zh_CN")
    point_change_records: List[Dict] = []

    # 为每个兑换记录生成积分变动记录（仅COMPLETED状态的）
    for i, exchange_record in enumerate(exchange_records):
        if exchange_record["status"] == "COMPLETED":
            exchange_id = exchange_ids[i]
            volunteer_id = exchange_record["volunteerId"]
            total_points = exchange_record["totalPoints"]
            order_time = exchange_record["orderTime"]

            # 变动时间为兑换时间（order_time），而不是处理时间
            # 这样才能保证在查询兑换时间点的积分时，能正确计算余额
            change_time = order_time

            # 变动原因和备注
            reason = random.choice(NOTES)
            note = exchange_record["note"]

            point_change_record = {
                "volunteerId": volunteer_id,
                "changePoints": -total_points,  # 扣减积分
                "changeType": "EXCHANGE_USE",
                "reason": reason,
                "relatedRecordId": exchange_id,
                "relatedRecordType": "EXCHANGE",
                "balanceAfter": None,  # 稍后计算
                "changeTime": change_time,
                "note": note,
            }

            point_change_records.append(point_change_record)

    return point_change_records


def calculate_all_balance_after(
    conn: PooledMySQLConnection | MySQLConnectionAbstract,
):
    """计算所有积分变动记录的余额"""
    cursor = conn.cursor()

    try:
        # 获取所有志愿者
        cursor.execute("SELECT id FROM volunteer WHERE deleted = FALSE")
        volunteer_ids = [row[0] for row in cursor.fetchall()]

        print(f"为 {len(volunteer_ids)} 个志愿者计算积分余额...")

        # 为每个志愿者计算余额
        for volunteer_id in tqdm(volunteer_ids, desc="计算积分余额"):
            # 获取该志愿者的所有积分变动记录，按时间排序
            cursor.execute(
                """
                SELECT id, change_points, change_time
                FROM point_change_record
                WHERE volunteer_id = %s
                ORDER BY change_time ASC, id ASC
                """,
                (volunteer_id,),
            )
            records = cursor.fetchall()

            # 计算余额
            balance = 0.0
            for record_id, change_points, change_time in records:
                balance += change_points
                # 更新余额
                cursor.execute(
                    "UPDATE point_change_record SET balance_after = %s WHERE id = %s",
                    (balance, record_id),
                )

        conn.commit()
        print("成功更新所有积分变动记录的余额")

    except mysql.connector.Error as err:
        print(f"计算积分余额时出错: {err}")
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
        # 兑换记录总数
        cursor.execute("SELECT COUNT(*) FROM exchange_record")
        total_exchanges = cursor.fetchone()[0]
        print(f"\n兑换记录总数: {total_exchanges}")

        # 积分变动记录总数
        cursor.execute(
            "SELECT COUNT(*) FROM point_change_record WHERE change_type = 'EXCHANGE_USE'"
        )
        total_exchange_point_changes = cursor.fetchone()[0]
        print(f"兑换积分变动记录总数: {total_exchange_point_changes}")

        # 兑换状态分布
        cursor.execute("SELECT status, COUNT(*) FROM exchange_record GROUP BY status")
        status_counts = cursor.fetchall()
        print("\n兑换状态分布:")
        for status, count in status_counts:
            print(f"  {status}: {count}")

        # 商品兑换统计
        cursor.execute(
            """
            SELECT p.name, COUNT(*) as count, SUM(er.number) as total_number
            FROM exchange_record er
            JOIN product p ON er.product_id = p.id
            GROUP BY p.id, p.name
            ORDER BY count DESC
        """
        )
        product_stats = cursor.fetchall()
        print("\n商品兑换统计:")
        for name, count, total_number in product_stats:
            print(f"  {name}: {count}次, 共{total_number}件")

        # 年度兑换统计
        cursor.execute(
            """
            SELECT YEAR(order_time) as year, COUNT(*) as count
            FROM exchange_record
            GROUP BY YEAR(order_time)
            ORDER BY year
        """
        )
        year_stats = cursor.fetchall()
        print("\n年度兑换统计:")
        for year, count in year_stats:
            print(f"  {year}年: {count}次")

        # 月度兑换统计
        cursor.execute(
            """
            SELECT YEAR(order_time) as year, MONTH(order_time) as month, COUNT(*) as count
            FROM exchange_record
            GROUP BY YEAR(order_time), MONTH(order_time)
            ORDER BY year, month
        """
        )
        month_stats = cursor.fetchall()
        print("\n月度兑换统计:")
        for year, month, count in month_stats:
            print(f"  {year}年{month}月: {count}次")

        # 兑换日统计
        cursor.execute(
            """
            SELECT DATE(order_time) as exchange_date, COUNT(*) as count
            FROM exchange_record
            GROUP BY DATE(order_time)
            ORDER BY exchange_date
        """
        )
        date_stats = cursor.fetchall()
        print("\n兑换日统计:")
        for exchange_date, count in date_stats:
            print(f"  {exchange_date}: {count}次")

    except mysql.connector.Error as err:
        print(f"查询统计信息时出错: {err}")
    finally:
        cursor.close()


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description="生成志愿服务平台兑换记录数据")
    parser.add_argument(
        "--clear-only",
        action="store_true",
        help="仅清空兑换记录表和相关的积分变动记录，不插入新数据",
    )
    args = parser.parse_args()

    print("志愿者系统兑换记录生成器（按兑换日批量生成）")

    conn = create_connection()

    try:
        if args.clear_only:
            cursor = conn.cursor()
            # 还原商品库存（会自动删除兑换记录）
            print("\n还原商品库存...")
            restore_product_stock(conn)
            # 删除兑换相关的积分变动记录
            cursor.execute(
                "DELETE FROM point_change_record WHERE change_type = 'EXCHANGE_USE'"
            )
            # 更新志愿者积分
            cursor.execute(
                "UPDATE volunteer SET points = (SELECT COALESCE(SUM(change_points), 0) FROM point_change_record WHERE volunteer_id = volunteer.id) WHERE deleted = FALSE"
            )
            cursor.execute("ALTER TABLE exchange_record AUTO_INCREMENT = 1")
            conn.commit()
            print("已清空兑换记录表和相关的积分变动记录")
            cursor.close()
        else:
            print("\n获取志愿者和商品数据...")
            volunteers = get_all_volunteers(conn)
            products = get_products(conn)

            if not volunteers:
                print("错误: 没有找到已认证的志愿者")
                return

            if not products:
                print("错误: 没有找到可用商品")
                return

            print("\n警告: 即将清空现有兑换记录和相关的积分变动记录数据...")
            confirm = input("确认继续? (y/n): ")
            if confirm.lower() != "y":
                print("操作已取消")
                return

            cursor = conn.cursor()
            # 还原商品库存（会自动删除兑换记录）
            print("\n还原商品库存...")
            restore_product_stock(conn)
            # 删除兑换相关的积分变动记录
            cursor.execute(
                "DELETE FROM point_change_record WHERE change_type = 'EXCHANGE_USE'"
            )
            cursor.execute("ALTER TABLE exchange_record AUTO_INCREMENT = 1")
            conn.commit()
            print("已清空兑换记录表和相关的积分变动记录")
            cursor.close()

            print("\n生成兑换记录...")
            exchange_records = generate_exchange_records(volunteers, products, conn)

            if not exchange_records:
                print("警告: 没有生成任何兑换记录（可能是志愿者积分不足）")
                return

            print(f"\n共生成 {len(exchange_records)} 条兑换记录")

            print("\n插入兑换记录...")
            exchange_ids = insert_exchange_records(conn, exchange_records)

            if not exchange_ids:
                print("错误: 插入兑换记录失败")
                return

            print("\n更新商品库存...")
            update_product_stock(conn, exchange_records)

            print("\n生成兑换积分变动记录...")
            point_change_records = generate_exchange_point_change_records(
                exchange_records, exchange_ids
            )

            print(f"共生成 {len(point_change_records)} 条积分变动记录")

            print("\n插入积分变动记录...")
            insert_point_change_records(conn, point_change_records)

            print("\n计算所有积分变动记录的余额...")
            calculate_all_balance_after(conn)

            print("\n更新志愿者积分字段...")
            update_volunteer_points(conn)

            print("\n统计信息:")
            print_statistics(conn)

    finally:
        conn.close()
        print("数据库连接已关闭")


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


if __name__ == "__main__":
    main()
