from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives.kdf.concatkdf import ConcatKDFHash
from cryptography.hazmat.primitives.kdf.scrypt import Scrypt
import base64
import os

# Generate a secure random key
key = os.urandom(64)  # 64 bytes for HS512
encoded_key = base64.urlsafe_b64encode(key).decode('utf-8')

print("Generated JWT Secret Key:")
print(encoded_key)

# Save to a file
with open("jwt-secret.key", "w") as key_file:
    key_file.write(encoded_key)
print("Key saved to jwt-secret.key")
