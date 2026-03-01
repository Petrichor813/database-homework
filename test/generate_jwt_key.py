import secrets
import base64
import string

def generate_key(length: int = 32) -> str:
    """
    生成适合JWT的Base64编码密钥
    
    Args:
        length: 字节长度，HS256需要至少32字节
    
    Returns:
        Base64编码的密钥字符串
    """
    # 生成随机字节
    random_bytes = secrets.token_bytes(length)
    
    # Base64编码
    base64_key = base64.urlsafe_b64encode(random_bytes).decode('utf-8')
    
    # 移除可能的=填充字符
    return base64_key.rstrip('=')

secret = generate_key(32)
print("=== JWT Base64密钥 ===")
print(secret)
print(f"长度: {len(secret)} 字符")