import socket

TCP_IP = '46.30.202.223'
TCP_PORT = 2429

BUFFER_SIZE = 1024
MAGIC_NUMBER = (1100).to_bytes(2, "little")
PROTOCOL_CMD_HEALTHCHECK = (99).to_bytes(1, "little")
PAYLOAD = b"\x2a"
NEW_LINE = b"\r\n"
PAYLOAD_SIZE = len(PAYLOAD).to_bytes(2, "little")

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((TCP_IP, TCP_PORT))
s.send(MAGIC_NUMBER)
s.send(PROTOCOL_CMD_HEALTHCHECK)
s.send(PAYLOAD_SIZE)
s.send(NEW_LINE)
s.send(PAYLOAD)
data = s.recv(BUFFER_SIZE)
s.close()

print("received data:", data)
