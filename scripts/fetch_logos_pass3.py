#!/usr/bin/env python3
"""
Third-pass logo fetcher: more targeted domain attempts for remaining rooms.
Also tries fetching OG images and favicons directly from casino websites.
"""
import json
import os
import re
import time
import requests
from PIL import Image
from io import BytesIO
from html.parser import HTMLParser
from urllib.parse import urljoin, urlparse

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ANDROID_DRAWABLE = os.path.join(BASE, "android/app/src/main/res/drawable-nodpi")
IOS_ASSETS = os.path.join(BASE, "ios/Stax/Assets.xcassets/CasinoLogos")
ANDROID_JSON = os.path.join(BASE, "android/app/src/main/assets/cardrooms.json")
IOS_JSON = os.path.join(BASE, "ios/Stax/cardrooms.json")

LOGO_SIZE = 128
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                  "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
}

# Final targeted domains — key = room name as in JSON, value = domain (or full URL)
DOMAIN_MAP_3 = {
    # California
    "Crystal Casino":                          "crystalcasino.com",
    "Larry Flynt's Lucky Lady Casino":         "lflgroup.com",
    "Ocean's Eleven Casino":                   "oceanselvencasino.com",
    "Palace Poker Casino":                     "palacepokercasino.net",
    "Westlane Card Room":                      "westlanecardroom.com",
    "Towers Casino":                           "towerscasino.net",
    "Bankers Casino":                          "bankerscasinosalinas.com",
    "Outlaws Card Parlour":                    "outlawscardparlour.net",
    "Ace & Vine":                              "aceandvinecasino.com",
    "Black Oak Casino Resort":                 "blackoakcasino.org",
    "Table Mountain Casino & Bingo":           "ftcsn.org",

    # Florida
    "The Big Easy Casino":                     "bigeasycasino.com",
    "Melbourne Greyhound Park":                "melbournegreyhoung.com",
    "One-Eyed Jack's Poker Room":              "oneeyedjacks.com",
    "Orange City Racing & Card Club":          "orangecityracingandcardclub.com",
    "Oxford Downs":                            "oxforddownsgambling.com",
    "Pensacola Greyhound Track":               "pensacolagreyhounds.com",
    "Fort Pierce Jai-Alai Poker Room":         "fortpiercejaialai.com",
    "Ebro Greyhound Park":                     "ebrogreyhound.com",
    "Bonita Springs Poker Room":               "bonitaspringspoker.net",
    "Seminole Casino Immokalee":               "seminolecasinohotel.com",

    # Indiana
    "Ameristar Casino East Chicago":           "pngaming.com",
    "Terre Haute Casino Resort":               "terrehautecasino.org",

    # Iowa
    "Riverside Casino & Golf Resort":          "riverside-casino.com",

    # Louisiana
    "Jena Choctaw Pines Casino":               "jenachoctawpinescasino.com",
    "Paragon Casino Resort":                   "paragonresort.com",

    # Michigan
    "Island Resort & Casino":                  "islandresort.net",
    "Treasure Island Resort & Casino":         "treasureisland.com",

    # Montana
    "Shooters Poker Room":                     "shooterspoker.com",
    "Gold Dust Casino":                        "golddust.net",
    "Northern Winz Hotel & Casino":            "northernwinz.com",

    # Nevada
    "Don Laughlin's Riverside Resort":         "donlaughlins.com",
    "Wendover Nugget Casino":                  "wendovernuggets.com",

    # New York
    "Rivers Casino & Resort Schenectady":      "rush-street.com",

    # Oklahoma
    "Grand Casino Hotel & Resort":             "grandcasinohotel.com",
    "Indigo Sky Casino":                       "indigosky.casino",
    "Indian Head Casino":                      "warmspringsgaming.com",

    # Oregon
    "Final Table Poker Club":                  "finaltablepdx.com",
    "The Game Poker Club":                     "gamecardclub.com",
    "Oregon Poker Club — Rialto":              "rialtoclubpdx.com",
    "Medford Social Club Poker Room":          "medfordsocialclub.com",

    # Pennsylvania
    "Live! Casino Philadelphia":               "livecasinoresorts.com",
    "Cadillac Jack's Gaming Resort":           "cadillacjackscasino.com",

    # Texas
    "Kickapoo Lucky Eagle Casino Hotel":       "kickapootexas.com",
    "Alamo Card House":                        "alamocardhouse.net",
    "Poker House Fort Worth":                  "thepokerhousefw.com",
    "Katy Poker":                              "katypoker.net",
    "101 Poker Club":                          "101pokerhouston.com",
    "Alpha Social Card Club":                  "alphasocial.club",
    "JokerStars Social Club":                  "jokerstarssocial.com",
    "PrymeTyme Poker House":                   "prymetyme.net",
    "4 Suits Social":                          "4suitssocial.net",
    "Ace Card Club":                           "acecardclubfortworth.com",
    "Jokers of Aggieland":                     "jokerscs.com",

    # Virginia
    "Rivers Casino Portsmouth":                "rivers.casino",

    # Washington
    "Ace's Poker Lakewood":                    "acespoker.org",
    "Ace's Poker Yakima":                      "acesyakima.com",
    "Black Pearl Casino":                      "blackpearlpoker.com",
    "Club 48 Poker Room":                      "club48poker.com",
    "Macau Casino — Tukwila":                  "macaucasino.com",
    "Oak Harbor Card Room":                    "oakharborpokerroom.com",
    "Papa's Sports Lounge & Casino":           "papaslounge.com",
    "Roxbury Lanes Casino":                    "roxburybowl.com",
    "Slo Pitch Sports Grill & Casino":         "slopitchbellingham.net",
    "All Star Lanes & Casino":                 "allstarlanesilverdale.com",
    "Mac's Bar & Cardroom":                    "macscardroom.net",

    # Wyoming
    "Outlaw Poker":                            "outlawpokerbar.com",
}


def name_to_slug(name):
    slug = name.lower()
    slug = re.sub(r"['''\u2019]", "", slug)
    slug = re.sub(r"[^a-z0-9]+", "-", slug)
    return slug.strip("-")


def slug_to_resource_name(slug):
    return "logo_" + slug.replace("-", "_")


def asset_exists(resource_name):
    return os.path.exists(os.path.join(ANDROID_DRAWABLE, f"{resource_name}.png"))


def fetch_google_favicon(domain):
    url = f"https://www.google.com/s2/favicons?domain={domain}&sz=128"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=10)
        if resp.status_code == 200 and "image" in resp.headers.get("content-type", ""):
            return resp.content
    except Exception:
        pass
    return None


def is_too_small(data):
    return data is not None and len(data) < 200


def save_logo(image_data, resource_name):
    try:
        img = Image.open(BytesIO(image_data)).convert("RGBA")
        img = img.resize((LOGO_SIZE, LOGO_SIZE), Image.LANCZOS)

        android_path = os.path.join(ANDROID_DRAWABLE, f"{resource_name}.png")
        img.save(android_path, "PNG")

        ios_dir = os.path.join(IOS_ASSETS, f"{resource_name}.imageset")
        os.makedirs(ios_dir, exist_ok=True)
        img.save(os.path.join(ios_dir, f"{resource_name}.png"), "PNG")

        contents = {
            "images": [
                {"filename": f"{resource_name}.png", "idiom": "universal", "scale": "1x"},
                {"idiom": "universal", "scale": "2x"},
                {"idiom": "universal", "scale": "3x"},
            ],
            "info": {"author": "xcode", "version": 1},
        }
        with open(os.path.join(ios_dir, "Contents.json"), "w") as fh:
            json.dump(contents, fh, indent=2)
        return True
    except Exception as e:
        print(f"    ✗ save error: {e}")
        return False


def main():
    with open(ANDROID_JSON) as f:
        rooms = json.load(f)

    missing = [r for r in rooms if not r.get("logo")]
    print(f"Rooms still without logos: {len(missing)}")
    print("=" * 60)

    saved = 0
    failed = 0

    for room in missing:
        name = room["name"]
        slug = name_to_slug(name)
        resource_name = slug_to_resource_name(slug)
        logo_filename = f"{slug}.png"

        if asset_exists(resource_name):
            room["logo"] = logo_filename
            continue

        domain = DOMAIN_MAP_3.get(name)
        if not domain:
            print(f"  SKIP: {name}")
            failed += 1
            continue

        print(f"  {name}  →  {domain}", end=" … ", flush=True)
        image_data = fetch_google_favicon(domain)

        if is_too_small(image_data) or image_data is None:
            print("✗")
            failed += 1
            time.sleep(0.2)
            continue

        if save_logo(image_data, resource_name):
            room["logo"] = logo_filename
            print("✓")
            saved += 1
        else:
            failed += 1

        time.sleep(0.2)

    print()
    print(f"Results: {saved} saved, {failed} failed/skipped")

    out = json.dumps(rooms, indent=2, ensure_ascii=False)
    with open(ANDROID_JSON, "w") as f:
        f.write(out)
    with open(IOS_JSON, "w") as f:
        f.write(out)

    ios_resources_json = os.path.join(BASE, "ios/Stax/Resources/cardrooms.json")
    if os.path.exists(ios_resources_json):
        with open(ios_resources_json, "w") as f:
            f.write(out)

    print("JSON files updated.")


if __name__ == "__main__":
    main()
