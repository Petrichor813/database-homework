import sys
import mysql.connector
from tqdm import tqdm

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "volunteer",
    "password": "volunteer1227",
    "database": "volunteer",
}


def create_connection():
    """创建数据库连接"""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        print("数据库连接成功")
        return conn
    except mysql.connector.Error as err:
        print(f"数据库连接错误: {err}")
        sys.exit(1)


def migrate_volunteer_points():
    """迁移志愿者积分数据"""
    conn = create_connection()
    cursor = conn.cursor()

    try:
        print("开始迁移志愿者积分数据...")

        # 检查 points 字段是否已存在
        cursor.execute("SHOW COLUMNS FROM volunteer LIKE 'points'")
        result = cursor.fetchone()

        if result is None:
            print("警告: volunteer 表中不存在 points 字段，请先添加该字段")
            print("请执行以下 SQL: ALTER TABLE volunteer ADD COLUMN points DOUBLE NOT NULL DEFAULT 0.0;")
            return

        # 使用单条 SQL 语句批量更新所有志愿者的积分
        print("正在批量更新志愿者积分...")
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
        affected_rows = cursor.rowcount
        conn.commit()
        print(f"成功更新 {affected_rows} 个志愿者的积分")

        # 显示统计信息
        cursor.execute("SELECT COUNT(*) FROM volunteer WHERE deleted = FALSE")
        total_volunteers = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM volunteer WHERE deleted = FALSE AND points > 0")
        volunteers_with_points = cursor.fetchone()[0]

        cursor.execute("SELECT SUM(points) FROM volunteer WHERE deleted = FALSE")
        total_points_sum = cursor.fetchone()[0]

        print("\n统计信息:")
        print(f"  总志愿者数: {total_volunteers}")
        print(f"  有积分的志愿者数: {volunteers_with_points}")
        print(f"  积分总和: {total_points_sum}")

        # 验证数据一致性
        cursor.execute("""
            SELECT 
                COUNT(*) as mismatch_count
            FROM volunteer v
            WHERE v.deleted = FALSE
            AND v.points <> (
                SELECT COALESCE(SUM(pcr.change_points), 0)
                FROM point_change_record pcr
                WHERE pcr.volunteer_id = v.id
            )
        """)
        mismatch_count = cursor.fetchone()[0]

        if mismatch_count > 0:
            print(f"\n警告: 发现 {mismatch_count} 个志愿者的积分与积分变动记录不一致！")
            print("建议检查数据或重新运行迁移脚本")
        else:
            print("\n数据一致性验证通过！")

    except mysql.connector.Error as err:
        print(f"迁移数据时出错: {err}")
        conn.rollback()
        raise
    finally:
        cursor.close()
        conn.close()
        print("数据库连接已关闭")


def main():
    """主函数"""
    print("志愿者积分数据迁移工具")
    print("该工具将从 point_change_record 表计算每个志愿者的积分总和，并更新到 volunteer.points 字段")
    print()

    confirm = input("确认继续? (y/n): ")
    if confirm.lower() != "y":
        print("操作已取消")
        return

    migrate_volunteer_points()


if __name__ == "__main__":
    main()
