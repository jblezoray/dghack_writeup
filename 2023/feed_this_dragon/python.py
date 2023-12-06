import requests
import json
import urllib.parse
import uuid
import copy
import sys
import threading
import time
import random
import os
import pytz
from datetime import datetime


def enable_debug():
    import logging  
    import http.client as http_client
    http_client.HTTPConnection.debuglevel = 1
    logging.basicConfig()
    logging.getLogger().setLevel(logging.DEBUG)
    requests_log = logging.getLogger("requests.packages.urllib3")
    requests_log.setLevel(logging.DEBUG)
    requests_log.propagate = True

def print_raw_data(data, name="", indent = ""):
    identifier = f"{name}: " if name != "" else ""
    if name in ["achievements", "items", "upgrades"]:
        print(f"{indent}{identifier}[")
        for d in data:
            print(f"{indent}  {d},")
        print(f"{indent}]")
    elif isinstance(data, list):
        print(f"{indent}{identifier}[")
        for d in data:
            print_raw_data(d, "", indent + " ")
        print(f"{indent}]")
    elif hasattr(data, "keys"):
        print(f"{indent}{identifier}{{")
        for key in data.keys():
            print_raw_data(data[key], key, indent + " ")
        print(f"{indent}}},")
    elif name == "delay":
        tz = pytz.timezone('Europe/Paris')
        ts = round(data /1000)
        date = datetime.fromtimestamp(ts, tz).isoformat()
        print(f"{indent}{identifier}{date},")
    else:
        print(f"{indent}{identifier}{data},")

def print_result(data):
    print_raw_data(data, name="result")


started_at_timstamp = round(time.time() * 1000)

def build_data(result):
    # data=[
    #     {"slug":"free", "name":"This One Is Free",    "description":"No need for thanks, really.",                                 "delay":1700161455629},
    #     {"slug":"works","name":"That''s How It Works","description":"Yes, just click here and there, it will be fine...",          "delay":1700161455630},
    #     {"slug":"hurt", "name":"It Must Have Hurt",   "description":"Sometimes getting hurt (a bit) is the best way to learn \041","delay":1700161455631}
    # ]
    data = []
    ts_counter = started_at_timstamp
    for achievement in result["achievements"]:
        if achievement["acquired"] is not True:
            continue
        line = {
            "slug": achievement["slug"],
            "name": achievement["name"],
            "description": achievement["description"],
            "delay": ts_counter,
        }
        ts_counter += 170000
        data.append(line)
    print_result(data)
    return data



def get_headers(data, session, new_session=None):
    url_encoded=str(urllib.parse.quote(json.dumps(data)))
    uuid=session if new_session is None else new_session
    return {
        'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/115.0',
        'Accept': '*/*',
        'Accept-Language': 'en-US,en;q=0.5',
        'Accept-Encoding': 'gzip, deflate',
        'Referer': 'http://feedthisdragon2.chall.malicecyber.com/',
        'Authorization': 'mynotsosecrettoken',
        'Session': session,
        'Update': 'true',
        'ItemUuid': '',
        'ShopUuid': '',
        'Content-Type': 'application/json',
        'Connection': 'keep-alive',
        'Cookie': f'uuid={uuid}; achievements={url_encoded}'
    }

def renew_session(session, data, new_session):
    headers = get_headers(data, session, new_session=new_session)
    r = requests.get(url=URL, headers=headers)
    return new_session,  r.json()


def get(session, data):
    headers = get_headers(data, session)
    r = requests.get(url=URL, headers=headers)
    try:
        return r.json()
    except requests.exceptions.JSONDecodeError:
        print(r.content)
        exit(0)

result = None

def get_result_loop(session, data):
    global result
    while True:
        result = get(session, data)
        time.sleep(0.5)


def get_result_daemon(session, data):
    x = threading.Thread(target=get_result_loop, args=(session, data), daemon=True)
    x.start()

def post(session, data, item_type=None, item_uuid=None, shop_uuid=None):
    headers = get_headers(data, session)
    if item_uuid is not None:
        headers["ItemUuid"] = item_uuid
    if shop_uuid is not None:
        headers["ShopUuid"] = shop_uuid
    r = requests.post(url=URL, headers=headers)
    return r.json()

boos_counter = {}
full_life_since = 0
kill_boos_after = 0

def find_a_boo_about_to_disapear(result):

    # update full_life_since
    global full_life_since
    if result["health"] >= result["max_health"] -2:
        full_life_since += 1
    else:
        full_life_since = 0

    # update round counters
    for boo in [item for item in result["items"] if item["type"] in ["lilboo", "midboo", "bigboo"]]:
        type, counter = boos_counter.get(boo["uuid"], (boo["type"], 0))
        boos_counter[boo["uuid"]] = (type, counter + 1)

    # remove the ones that are not any more in the list
    boos_uuids_to_remove = []
    for boo_uuid in list(boos_counter.keys()):
        exists = len([item for item in result["items"] if item["uuid"] == boo_uuid])
        if not exists :
            boos_uuids_to_remove.append(boo_uuid)
    removed = False
    for boo_uuid in boos_uuids_to_remove:
        print("*" * 800)
        print(f"boo was removed :-( {boo_uuid}   @ {boos_counter[boo_uuid]}")
        print("*" * 800)
        removed = True
        boos_counter.pop(boo_uuid, None)

    # boo removed 
    global kill_boos_after
    if removed:
        kill_boos_after = max(0, kill_boos_after - 2)
    elif full_life_since != 0 and full_life_since % 15 ==0:
        kill_boos_after += 1
    print(f"full_life_since {full_life_since}")
    print(f"kill_boos_after {kill_boos_after}")

    # find the oldest of the old ones.
    old_ones_oldest = None
    old_ones_counter = 0
    for boo_uuid in list(boos_counter.keys()):
        type, counter = boos_counter[boo_uuid]
        if counter-kill_boos_after>old_ones_counter:
            old_ones_oldest = boo_uuid
            old_ones_counter = counter-kill_boos_after
    boos_counter.pop(old_ones_oldest, None)
    return old_ones_oldest


def has_achievement(result, slug):
    for achievement in result["achievements"]:
        if achievement["slug"] == slug:
            return achievement["acquired"]
    return False

def find_item(result):

    boos = [item["uuid"] for item in result["items"] if item["type"] in ["lilboo", "midboo", "bigboo"]]
    foods = [item["uuid"] for item in result["items"] if item["type"] in ["veggy", "food", "candy", "burger"]]
    lifes = [item["uuid"] for item in result["items"] if item["type"] in ["life"]]
    coins = [item["uuid"] for item in result["items"] if item["type"] in ["coin", "gem"]]
    secrets = [item["uuid"] for item in result["items"] if item["type"] in ["secret"]]
    nyans = [item["uuid"] for item in result["items"] if item["type"] in ["nyan"]]
    traps = [item["uuid"] for item in result["items"] if item["type"] in ["trap", "fox"]]
    other = [item["uuid"] for item in result["items"] if item["uuid"] not in [*traps, *foods, *lifes, *coins, *boos, *secrets, *nyans]]

    if len(other)>1:
        other_uuid = random.choice(other)
        print("#" * 2000)
        print(f"other : {other_uuid}")
        print("#" * 2000)

    boo_about_to_disapear = find_a_boo_about_to_disapear(result)
    if boo_about_to_disapear is not None:
        return boo_about_to_disapear
    
    if len(lifes) >= 1 and result["health"] < result["max_health"]:
        return random.choice(lifes)

    if len(nyans)>0 and not has_achievement(result, "saw"):
        return random.choice(nyans)

    if len(secrets)>0 and not has_achievement(result, "secret"):
        return random.choice(secrets)

    if len(coins)>0 or len(foods)>0:
        if has_achievement(result, "bigger") and len(coins)>0:
            return random.choice(coins)
        else:
            return random.choice([*coins, *foods])
    return None

def find_upgrade(result):

    def get(slug):
        for r in result["upgrades"]:
            if r["slug"] == slug:
                return r
        return None
    
    if result["coin"] == result["bag"]:
        return get("bag")["uuid"]

    if not has_achievement(result, "gold"):
        if get("greed")["cost"] < result["coin"]:
            return get("greed")["uuid"]
        return None
    
    if result["max_health"] < 20 and get("hard")["cost"] <= result["coin"]:
        return get("hard")["uuid"]
    
    if has_achievement(result, "bigger"):
        return None

    for upgrade in result["upgrades"]:
        if upgrade["cost"] < result["coin"] and upgrade["slug"] not in ["bag", "hard"]:
            return upgrade["uuid"]
    return None


SESSION="372667d0-805b-41e7-b63b-7e03c9e2550c"
URL="http://feedthisdragon3.chall.malicecyber.com/api/v1"

if len(sys.argv) > 1:
    new_session = sys.argv[1]
else:
    new_session = str(uuid.uuid4())
new_session, result = renew_session(SESSION, [], new_session)
get_result_daemon(new_session, [])
shop_uuid = None
item_uuids = []
data = []
while True:

    
    if shop_uuid is None:
        result = post(new_session, data, shop_uuid=shop_uuid)
    else:
        result = get(new_session, data)
    threads = []
    for item_uuid in item_uuids:
        t = threading.Thread(target=post, args=[new_session, data], kwargs={
            "item_uuid": item_uuid, "shop_uuid": shop_uuid
        })
        
        t.start()
        threads.append(t)
    for t in threads:
        t.join(timeout=3.0)

    print("#" * 175)

    shop_uuid = find_upgrade(result)
    item_uuids = []
    for i in range(20):
        item_uuid = find_item(result)
        if item_uuid is not None:
            item_uuids.append(item_uuid)
            result["items"] = [i for i in result["items"] if i["uuid"]!=item_uuid]
    data = build_data(result)

    del result["items"]
    print_result(result)

    if result["health"] <= 0:
        print(" :-( " * 80)
        sys.exit()
