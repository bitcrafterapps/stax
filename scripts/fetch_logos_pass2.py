#!/usr/bin/env python3
"""
Second-pass logo fetcher: tries alternate/parent-brand domains for rooms
that still lack logos after the first pass.
"""
import json
import os
import re
import time
import requests
from PIL import Image
from io import BytesIO

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

# Alternate/corrected domains for rooms that failed in pass 1.
# Key = room name exactly as in cardrooms.json.
DOMAIN_MAP_2 = {
    # California
    "Crystal Casino":                        "crystalcasinocompton.com",
    "Larry Flynt's Lucky Lady Casino":       "larryflynt.com",
    "Ocean's Eleven Casino":                 "oceanselevenpoker.com",
    "Lake Elsinore Casino":                  "lakeelsinore.com",
    "Palace Poker Casino":                   "palace-poker.com",
    "Stars Casino":                          "starscasino.com",
    "Westlane Card Room":                    "westlanecardroom.net",
    "Diamond Jim's Casino":                  "diamondjims.com",
    "Towers Casino":                         "towerscasinonv.com",
    "Bankers Casino":                        "bankercasino.com",
    "Outlaws Card Parlour":                  "outlawscardparlour.com",
    "Ace & Vine":                            "aceandvine.net",
    "Table Mountain Casino & Bingo":         "tablemountaincasino.com",
    "Quechan Resort Casino":                 "quechan.com",
    "Black Oak Casino Resort":               "blackoakcasino.net",

    # Colorado
    "Horseshoe Black Hawk Casino":           "caesars.com",
    "Hard Rock Casino Rockford":             "hardrock.com",

    # Florida
    "Casino Miami Jai-Alai":                 "casinomiami.net",
    "Melbourne Greyhound Park":              "melbournegreyhound.com",
    "One-Eyed Jack's Poker Room":            "oneeyedjacksflorida.com",
    "Orange City Racing & Card Club":        "ocracingcardclub.com",
    "Fort Pierce Jai-Alai Poker Room":       "fpjaialai.com",
    "Ebro Greyhound Park":                   "ebrogreyhoundpark.com",
    "Bonita Springs Poker Room":             "bonitasprings-poker.com",
    "Seminole Casino Immokalee":             "seminoleimmokalee.com",
    "The Big Easy Casino":                   "bigeasyhallandale.com",

    # Illinois
    "Caesars Southern Indiana":              "caesars.com",
    "Hard Rock Casino Rockford":             "hardrockcasino.com",

    # Indiana
    "Caesars Southern Indiana":              "caesars.com",
    "Horseshoe Indianapolis":                "caesars.com",
    "Hard Rock Casino Northern Indiana":     "hardrock.com",
    "Ameristar Casino East Chicago":         "penn.com",
    "Terre Haute Casino Resort":             "terrehautecasinoresort.com",

    # Iowa
    "Riverside Casino & Golf Resort":        "riversidemycasino.com",

    # Louisiana
    "Caesars New Orleans":                   "caesars.com",
    "Golden Nugget Lake Charles":            "goldennugget.com",
    "Jena Choctaw Pines Casino":             "jenachoctawpines.com",
    "Paragon Casino Resort":                 "paragoncasino.com",

    # Maryland
    "Hollywood Casino Perryville":           "hollywoodcasino.com",

    # Michigan
    "Island Resort & Casino":                "islandresort.com",
    "Treasure Island Resort & Casino":       "treasureislandcasino.com",

    # Mississippi
    "Golden Nugget Biloxi":                  "goldennugget.com",
    "Gold Strike Casino Resort":             "goldstrike.com",
    "Ameristar Casino Vicksburg":            "ameristar.com",

    # Missouri
    "Horseshoe Casino St. Louis":            "caesars.com",

    # Montana
    "Shooters Poker Room":                   "shootersbillings.com",
    "Gold Dust Casino":                      "golddust-billings.com",
    "Northern Winz Hotel & Casino":          "northernwinz.com",

    # Nevada
    "Don Laughlin's Riverside Resort":       "laughlinriverside.com",
    "Wendover Nugget Casino":                "wendover-nevada.com",
    "Hard Rock Hotel & Casino Las Vegas":    "hardrock.com",

    # New Mexico
    "Isleta Resort & Casino":                "isleta.com",

    # New York
    "Rivers Casino & Resort Schenectady":    "riverscasino.com",

    # North Carolina
    "Harrah's Cherokee Valley River Casino": "caesars.com",

    # Ohio
    "Hollywood Casino Columbus":             "hollywoodcasino.com",
    "Hollywood Casino Toledo":               "hollywoodcasino.com",

    # Oklahoma
    "River Spirit Casino Resort":            "windcreekhospitality.com",
    "Grand Casino Hotel & Resort":           "grandresort.com",
    "Indigo Sky Casino":                     "wyandottecasino.com",
    "Cherokee Casino West Siloam Springs":   "cherokeenation.com",

    # Oregon
    "Indian Head Casino":                    "warmsprings.nsn.us",
    "Final Table Poker Club":                "finaltablepoker.com",
    "The Game Poker Club":                   "thegamecardclub.com",
    "Oregon Poker Club — Rialto":            "rialtoclub.com",
    "Full House Poker":                      "fullhousepoker.com",
    "Medford Social Club Poker Room":        "medfordsocial.com",
    "Grants Pass Poker Room":                "grantspasspokerroom.com",

    # Pennsylvania
    "Live! Casino Philadelphia":             "livecasino.com",
    "Hollywood Casino at the Meadows":       "hollywoodcasino.com",
    "Hollywood Casino Morgantown":           "hollywoodcasino.com",
    "Bally's Twin River Casino Hotel":       "ballys.com",
    "Cadillac Jack's Gaming Resort":         "cadillacjacksgamingresort.com",

    # Texas
    "Kickapoo Lucky Eagle Casino Hotel":     "kickapooluckyeagle.net",
    "Alamo Card House":                      "alamocard.house",
    "Poker House Fort Worth":                "pokerhouse.gg",
    "Aggieland Poker Club":                  "aggielandpokerclub.com",
    "Katy Poker":                            "katypokerclub.com",
    "101 Poker Club":                        "101poker.com",
    "Alpha Social Card Club":                "alphasocialcc.com",
    "JokerStars Social Club":                "jokerstars.com",
    "VIP Social Club":                       "vipsocialclub.com",
    "PrymeTyme Poker House":                 "prymetyme.poker",
    "4 Suits Social":                        "4suits.social",
    "Ace Card Club":                         "acefortworth.com",
    "Jokers of Aggieland":                   "jokersofaggieland.gg",

    # Virginia
    "Rivers Casino Portsmouth":              "riverscasino.com",
    "Caesars Virginia":                      "caesars.com",
    "Hard Rock Hotel & Casino Bristol":      "hardrock.com",

    # Washington tribal
    "7 Cedars Casino":                       "7cedars.com",
    "Legends Casino Hotel":                  "legendscasino.com",

    # Washington card rooms
    "Ace's Poker Lakewood":                  "acespoker.net",
    "Ace's Poker Yakima":                    "acespoker.net",
    "Black Pearl Casino":                    "blackpearlcasino.com",
    "Club 48 Poker Room":                    "club48.com",
    "Macau Casino — Tukwila":                "macaucasino.us",
    "Oak Harbor Card Room":                  "oakharborpoker.com",
    "Papa's Sports Lounge & Casino":         "papasportslounge.com",
    "Roxbury Lanes Casino":                  "roxbury-lanes.com",
    "Slo Pitch Sports Grill & Casino":       "slopitchsports.com",
    "All Star Lanes & Casino":               "allstarlanes.net",
    "Mac's Bar & Cardroom":                  "macscardroom.com",

    # West Virginia
    "Mardi Gras Casino & Resort":            "mardigrashotel.com",

    # Wyoming
    "Ace High Social Club":                  "acehighsocial.com",
    "Outlaw Poker":                          "outlawpoker.com",
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


def fetch_clearbit(domain):
    url = f"https://logo.clearbit.com/{domain}?size={LOGO_SIZE}"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=10)
        if resp.status_code == 200:
            ct = resp.headers.get("content-type", "")
            if "image" in ct and "svg" not in ct:
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

        domain = DOMAIN_MAP_2.get(name)
        if not domain:
            print(f"  SKIP (no alternate domain): {name}")
            failed += 1
            continue

        print(f"  {name}  →  {domain}", end=" … ", flush=True)

        image_data = fetch_clearbit(domain)
        source = "Clearbit"
        if is_too_small(image_data) or image_data is None:
            image_data = fetch_google_favicon(domain)
            source = "Google"

        if is_too_small(image_data) or image_data is None:
            print("✗")
            failed += 1
            time.sleep(0.2)
            continue

        if save_logo(image_data, resource_name):
            room["logo"] = logo_filename
            print(f"✓  [{source}]")
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
