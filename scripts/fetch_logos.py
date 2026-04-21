#!/usr/bin/env python3
"""
Fetches logos for card rooms that don't have logos assigned yet.
Uses Clearbit Logo API as primary source, Google Favicon service as fallback.
Saves PNGs to Android drawable-nodpi and creates iOS imageset bundles.
Updates both cardrooms.json files.
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

# Target size for logo images
LOGO_SIZE = 128

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                  "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
}

# Map of card room name → official website domain.
# Clearbit logo API: https://logo.clearbit.com/{domain}
# If Clearbit fails, we fall back to Google favicon service.
DOMAIN_MAP = {
    # ── ARIZONA ──────────────────────────────────────────────────────────────
    "Harrah's Ak-Chin Casino":               "harrahs.com",
    "Desert Diamond Casino West Valley":     "ddcaz.com",
    "Desert Diamond Casino Tucson":          "ddcaz.com",
    "Desert Diamond Casino Sahuarita":       "ddcaz.com",
    "Yavapai Casino":                        "yavapaicasino.com",
    "Apache Gold Casino Resort":             "apachegoldcasinoresort.com",
    "Hon-Dah Resort Casino":                 "hon-dah.com",
    "Casino of the Sun":                     "casinoofthesun.com",

    # ── CALIFORNIA – Standalone Card Rooms ───────────────────────────────────
    "500 Club Casino":                       "500clubcasino.com",
    "Crystal Casino":                        "crystalcasinohotel.com",
    "Larry Flynt's Lucky Lady Casino":       "luckylady.com",
    "Ocean's Eleven Casino":                 "oceanselevencasino.com",
    "Lake Elsinore Casino":                  "lakeelsinorecasino.com",
    "Player's Casino":                       "playerscasino.com",
    "Parkwest Casino 580":                   "parkwestcasinos.com",
    "Palace Poker Casino":                   "palacepokercasino.com",
    "Livermore Casino":                      "livermorecasino.com",
    "Kings Card Club":                       "kingscardclub.com",
    "Stars Casino":                          "starscasinostockton.com",
    "Westlane Card Room":                    "westlanecardroom.com",
    "Diamond Jim's Casino":                  "diamondjimscasino.com",
    "Casino Chico":                          "casinochico.com",
    "Casino 99":                             "casino99.com",
    "Towers Casino":                         "towerscasino.com",
    "Limelight Card Room":                   "limelightcardroom.com",
    "The Nineteenth Hole":                   "nineteenth-hole.com",
    "Bankers Casino":                        "bankerscasino.com",
    "Outlaws Card Parlour":                  "outawscardparlour.com",
    "Ace & Vine":                            "aceandvine.com",
    "Tres Lounge and Casino":                "treslounge.com",

    # ── CALIFORNIA – Tribal Casinos ───────────────────────────────────────────
    "Yaamava' Resort & Casino at San Manuel": "yaamava.com",
    "Chumash Casino Resort":                 "chumashcasino.com",
    "River Rock Casino":                     "riverrockcasino.com",
    "Black Oak Casino Resort":               "blackoakcasino.com",
    "Table Mountain Casino & Bingo":         "table-mountain.net",
    "Soboba Casino Resort":                  "soboba.com",
    "Fantasy Springs Resort Casino":         "fantasyspringsresort.com",
    "Spotlight 29 Casino":                   "spotlight29.com",
    "Pala Casino Spa & Resort":              "palacasino.com",
    "Valley View Casino & Hotel":            "valleyviewcasino.com",
    "Agua Caliente Casino Cathedral City":   "aguacalientecasinos.com",
    "Agua Caliente Casino Rancho Mirage":    "aguacalientecasinos.com",
    "Win-River Resort & Casino":             "winriver.com",
    "Quechan Resort Casino":                 "playquechan.com",
    "Harrah's Northern California Casino":   "harrahs.com",

    # ── COLORADO ────────────────────────────────────────────────────────────
    "Bally's Black Hawk Casino":             "ballysblackhawk.com",
    "Horseshoe Black Hawk Casino":           "horseshoe.com",
    "Midnight Rose Hotel & Casino":          "midnightrose.com",

    # ── DELAWARE ────────────────────────────────────────────────────────────
    "Delaware Park Casino & Racing":         "delawarepark.com",

    # ── FLORIDA – Parimutuel Card Rooms ──────────────────────────────────────
    "The Big Easy Casino":                   "thebigeasy.com",
    "bestbet St. Augustine":                 "bestbet.com",
    "Casino Miami Jai-Alai":                 "casinomiami.com",
    "The Casino at Dania Beach":             "casinodaniabeach.com",
    "Harrah's Pompano Beach":                "harrahs.com",
    "Melbourne Greyhound Park":              "melbournegp.com",
    "One-Eyed Jack's Poker Room":            "oneeyedjackssarasota.com",
    "Orange City Racing & Card Club":        "orangecityracing.com",
    "Oxford Downs":                          "oxforddownsgaming.com",
    "Pensacola Greyhound Track":             "pensacolagreyhoundpark.com",
    "Calder Casino":                         "caldercasino.com",
    "Silks at Tampa Bay Downs":              "tampabaydowns.com",
    "Fort Pierce Jai-Alai Poker Room":       "fpjai.com",
    "Ebro Greyhound Park":                   "ebrogreyhound.com",
    "Bonita Springs Poker Room":             "bonitaspringspokerroom.com",

    # ── FLORIDA – Tribal ──────────────────────────────────────────────────
    "Seminole Casino Coconut Creek":         "seminolecasinococonutcreek.com",
    "Seminole Casino Immokalee":             "seminolecasino.com",
    "Seminole Classic Casino":               "seminoleclassiccasino.com",
    "Miccosukee Resort & Gaming Center":     "miccosukee.com",
    "Wind Creek Miami":                      "windcreekhospitality.com",

    # ── ILLINOIS ─────────────────────────────────────────────────────────────
    "Hollywood Casino Aurora":               "hollywoodcasinoaurora.com",
    "Harrah's Joliet Casino & Hotel":        "harrahsjoliet.com",
    "Hollywood Casino Joliet":               "hollywoodcasinojoliet.com",
    "Bally's Quad Cities Casino & Hotel":    "ballysquadcities.com",
    "Par-A-Dice Hotel Casino":               "paradicecasino.com",
    "Hard Rock Casino Rockford":             "hardrockcasinorockford.com",
    "Wind Creek Chicago Southland":          "windcreekhospitality.com",
    "DraftKings at Casino Queen":            "casinoqueen.com",

    # ── INDIANA ──────────────────────────────────────────────────────────────
    "Caesars Southern Indiana":              "caesarssouthernindiana.com",
    "Horseshoe Indianapolis":                "horseshoeindianapolis.com",
    "Four Winds South Bend":                 "fourwindscasino.com",
    "Hard Rock Casino Northern Indiana":     "hardrockcasinoni.com",
    "Hollywood Casino Lawrenceburg":         "hollywoodlawrenceburg.com",
    "Ameristar Casino East Chicago":         "ameristar.com",
    "Blue Chip Casino Hotel & Spa":          "bluechipcasino.com",
    "Rising Star Casino Resort":             "risingstarcasino.com",
    "Belterra Casino Resort":               "belterracasino.com",
    "Harrah's Hoosier Park":                 "harrahshoosierpark.com",
    "Bally's Evansville":                    "ballysevansville.com",
    "Terre Haute Casino Resort":             "terrehautecasino.com",
    "French Lick Resort Casino":             "frenchlick.com",

    # ── IOWA ─────────────────────────────────────────────────────────────────
    "Grand Falls Casino Resort":             "grandfallscasino.com",
    "Riverside Casino & Golf Resort":        "riversidecasinoresort.com",

    # ── KANSAS ───────────────────────────────────────────────────────────────
    "Hollywood Casino at Kansas Speedway":   "hollywoodkansasspeedway.com",

    # ── LOUISIANA ────────────────────────────────────────────────────────────
    "Caesars New Orleans":                   "caesarsneworleans.com",
    "Bally's Shreveport":                    "ballysshreveport.com",
    "Boomtown Casino New Orleans":           "boomtownneworleans.com",
    "Golden Nugget Lake Charles":            "goldennuggetlakecharles.com",
    "Horseshoe Bossier City":                "horseshoebossiercity.com",
    "Jena Choctaw Pines Casino":             "jenachoctaw.com",
    "Paragon Casino Resort":                 "paragoncasinoresort.com",

    # ── MAINE ────────────────────────────────────────────────────────────────
    "Hollywood Casino Bangor":               "hollywoodcasinobangor.com",

    # ── MARYLAND ─────────────────────────────────────────────────────────────
    "Hollywood Casino Perryville":           "hollywoodperryville.com",

    # ── MICHIGAN ─────────────────────────────────────────────────────────────
    "Soaring Eagle Casino & Resort":         "soaringeaglecasino.com",
    "Gun Lake Casino":                       "gunlakecasino.com",
    "Island Resort & Casino":                "islandresortcasino.com",
    "Odawa Casino Resort":                   "odawacasino.com",

    # ── MINNESOTA ────────────────────────────────────────────────────────────
    "Treasure Island Resort & Casino":       "ticasino.com",
    "Shooting Star Casino":                  "starcasino.com",
    "Seven Clans Casino Warroad":            "sevenclanscasino.com",
    "Northern Lights Casino":                "northernlightscasino.com",
    "Fortune Bay Resort Casino":             "fortunebay.com",
    "Cedar Lakes Casino Hotel":              "cedarlakescasino.com",

    # ── MISSISSIPPI ──────────────────────────────────────────────────────────
    "Hard Rock Hotel & Casino Biloxi":       "hardrockbiloxi.com",
    "Golden Nugget Biloxi":                  "goldennuggetbiloxi.com",
    "Boomtown Casino Biloxi":                "boomtownbiloxi.com",
    "Hollywood Casino Gulf Coast":           "hollywoodgulfcoast.com",
    "Horseshoe Casino Tunica":               "horseshoetunica.com",
    "Gold Strike Casino Resort":             "goldstrikecasino.com",
    "Pearl River Resort":                    "pearlriverresort.com",
    "Ameristar Casino Vicksburg":            "ameristar.com",

    # ── MISSOURI ─────────────────────────────────────────────────────────────
    "Ameristar Casino St. Charles":          "ameristar.com",
    "Horseshoe Casino St. Louis":            "horseshoestlouis.com",

    # ── MONTANA ──────────────────────────────────────────────────────────────
    # Large Montana gaming chains
    "Northern Winz Hotel & Casino":          "northernwinzcasino.com",
    "Shooters Poker Room":                   "shooterspokerbillings.com",
    "Gold Dust Casino":                      "golddust.com",
    "Montana Club Casino":                   "montanaclub.com",
    "Stockman's Bar & Casino":               "stockmansbar.com",
    "Northern Winz Hotel & Casino":          "northernwinzcasino.com",

    # ── NEBRASKA ─────────────────────────────────────────────────────────────
    "Grand Island Casino Resort":            "grandislandcasinoresort.com",

    # ── NEVADA ───────────────────────────────────────────────────────────────
    "Boulder Station Casino":                "boulderstation.com",
    "Santa Fe Station Hotel & Casino":       "santafestation.com",
    "Palace Station Hotel & Casino":         "palacestation.com",
    "Suncoast Hotel & Casino":               "suncoastcasino.com",
    "Aliante Casino & Hotel":                "aliantegaming.com",
    "Westgate Las Vegas Resort & Casino":    "westgatelasvegas.com",
    "M Resort Spa Casino":                   "themresort.com",
    "Harrah's Laughlin":                     "harrahslaughlin.com",
    "Aquarius Casino Resort":                "aquariuscasinoresort.com",
    "Golden Nugget Laughlin":                "goldennugget.com",
    "Don Laughlin's Riverside Resort":       "riversideresortlaughlin.com",
    "Edgewater Hotel & Casino":              "edgewater-casino.com",
    "Silver Legacy Resort Casino":           "silverlegacy.com",
    "Circus Circus Reno":                    "circuscircusreno.com",
    "Alamo Casino":                          "alamocasino.net",
    "Wendover Nugget Casino":                "wendovernugget.com",
    "Hard Rock Hotel & Casino Las Vegas":    "hardrocklasvegas.com",

    # ── NEW JERSEY ───────────────────────────────────────────────────────────
    "Caesars Atlantic City":                 "caesarsac.com",
    "Golden Nugget Atlantic City":           "goldennuggetac.com",
    "Resorts Casino Hotel":                  "resortsac.com",
    "Bally's Atlantic City":                 "ballysatlanticcity.com",

    # ── NEW MEXICO ───────────────────────────────────────────────────────────
    "Sandia Resort & Casino":                "sandiacasino.com",
    "Isleta Resort & Casino":                "isletacasino.com",
    "Inn of the Mountain Gods Resort & Casino": "innofthemountaingods.com",
    "Route 66 Casino Hotel":                 "rt66casino.com",
    "Buffalo Thunder Resort & Casino":       "buffalothunderresort.com",

    # ── NEW YORK ─────────────────────────────────────────────────────────────
    "Resorts World Catskills":               "rwcatskills.com",
    "Rivers Casino & Resort Schenectady":    "riverscasinoschenectady.com",
    "del Lago Resort & Casino":              "dellagoresort.com",
    "Tioga Downs Casino Resort":             "tiogadowns.com",
    "Seneca Niagara Resort & Casino":        "senecaniagaracasino.com",
    "Seneca Allegany Resort & Casino":       "senecaalleganycasino.com",
    "Akwesasne Mohawk Casino Resort":        "mohawkcasino.com",

    # ── NORTH CAROLINA ───────────────────────────────────────────────────────
    "Harrah's Cherokee Valley River Casino": "harrahscherokeevalleyriver.com",

    # ── NORTH DAKOTA ─────────────────────────────────────────────────────────
    "4 Bears Casino & Lodge":                "4bearscasino.com",
    "Spirit Lake Casino & Resort":           "spiritlakecasino.com",
    "Sky Dancer Casino & Resort":            "skydancercasino.com",

    # ── OHIO ─────────────────────────────────────────────────────────────────
    "Hard Rock Casino Cincinnati":           "hardrockcincinnati.com",
    "Hollywood Casino Columbus":             "hollywoodcolumbus.com",
    "Hollywood Casino Toledo":               "hollywoodtoledo.com",
    "Jack Cincinnati Casino":                "jackentertainment.com",

    # ── OKLAHOMA ─────────────────────────────────────────────────────────────
    "River Spirit Casino Resort":            "riverspiritcasino.com",
    "Cherokee Casino West Siloam Springs":   "cherokeecasino.com",
    "Choctaw Casino Pocola":                 "choctawcasinos.com",
    "Downstream Casino Resort":              "downstreamcasino.com",
    "Grand Casino Hotel & Resort":           "grandcasinoresort.com",
    "Indigo Sky Casino":                     "indigoskycasino.com",

    # ── OREGON ───────────────────────────────────────────────────────────────
    "Seven Feathers Casino Resort":          "sevenfeathers.com",
    "Three Rivers Casino Resort":            "threeriverscasino.com",
    "Wildhorse Resort & Casino":             "wildhorseresort.com",
    "Indian Head Casino":                    "indianheadcasino.com",
    "Kla-Mo-Ya Casino":                      "klamoya.com",
    "Final Table Poker Club":                "finaltablepdx.com",
    "The Game Poker Club":                   "thegamepdx.com",
    "Oregon Poker Club — Rialto":            "oregonpokerclub.com",
    "Full House Poker":                      "fullhousepokereugene.com",
    "Bend Poker Room":                       "bendpokerroom.com",
    "Medford Social Club Poker Room":        "medfordsocialclubpoker.com",
    "Grants Pass Poker Room":                "grantspasspoker.com",

    # ── PENNSYLVANIA ─────────────────────────────────────────────────────────
    "Wind Creek Bethlehem":                  "windcreekbethlehem.com",
    "Live! Casino Philadelphia":             "livecasinophiladelphia.com",
    "Hollywood Casino at the Meadows":       "hollywoodatthemeadows.com",
    "Mohegan Sun Pocono":                    "mohegansunpocono.com",
    "Hollywood Casino Morgantown":           "hollywoodmorgantown.com",
    "Valley Forge Casino Resort":            "vfcasino.com",
    "Mount Airy Casino Resort":              "mountairycasino.com",

    # ── RHODE ISLAND ─────────────────────────────────────────────────────────
    "Bally's Twin River Casino Hotel":       "ballystwinriver.com",
    "Bally's Tiverton Casino Hotel":         "ballystiverton.com",

    # ── SOUTH DAKOTA ─────────────────────────────────────────────────────────
    "Silverado Franklin Historic Hotel & Gaming": "silveradofranklin.com",
    "Cadillac Jack's Gaming Resort":         "cadillackjacks.com",
    "Saloon No. 10":                         "saloon10.com",
    "Deadwood Mountain Grand":               "deadwoodmountaingrand.com",
    "Royal River Casino":                    "royalrivercasino.com",
    "Grand River Casino & Resort":           "grandrivercasino.com",

    # ── TEXAS ────────────────────────────────────────────────────────────────
    "Kickapoo Lucky Eagle Casino Hotel":     "kickapooluckyeagle.com",
    "Shuffle 512":                           "shuffle512.com",
    "Alamo Card House":                      "alamocardhouse.com",
    "SA Card House":                         "sacardhouse.com",
    "Poker House Fort Worth":                "pokerhousefw.com",
    "Aggieland Poker Club":                  "aggielandpoker.com",
    "Cypress Poker Club":                    "cypresspokerclub.com",
    "Katy Poker":                            "katypoker.com",
    "101 Poker Club":                        "101pokerclub.com",
    "Alpha Social Card Club":                "alphasocialcardclub.com",
    "JokerStars Social Club":                "jokerstarssocialclub.com",
    "VIP Social Club":                       "vipsocialclubhouston.com",
    "PrymeTyme Poker House":                 "prymetymehouse.com",
    "4 Suits Social":                        "4suitssocial.com",
    "Ace Card Club":                         "acecardclubfw.com",
    "Jokers of Aggieland":                   "jokersofaggieland.com",

    # ── VIRGINIA ─────────────────────────────────────────────────────────────
    "Rivers Casino Portsmouth":              "riverscasinoportsmouth.com",
    "Caesars Virginia":                      "caesarsvirginia.com",
    "Hard Rock Hotel & Casino Bristol":      "hardrockcasinobristol.com",

    # ── WASHINGTON – Tribal ───────────────────────────────────────────────────
    "7 Cedars Casino":                       "7cedarscasino.com",
    "Legends Casino Hotel":                  "legendscasinohotel.com",
    "Little Creek Casino Resort":            "little-creek.com",
    "Northern Quest Resort & Casino":        "northernquest.com",
    "Chewelah Casino":                       "chewelahcasino.com",

    # ── WASHINGTON – Standalone Card Rooms ───────────────────────────────────
    "Ace's Poker Lakewood":                  "acespoker.com",
    "Ace's Poker Yakima":                    "acespoker.com",
    "Black Pearl Casino":                    "blackpearlcasinospokane.com",
    "Lilac Lanes & Casino":                  "lilaclanes.com",
    "Buzz Inn Steakhouse & Casino — Arlington": "buzzinn.com",
    "Buzz Inn Steakhouse & Casino — Snohomish": "buzzinn.com",
    "Casino Caribbean Kirkland":             "casinocaribbean.com",
    "Club 48 Poker Room":                    "club48yakima.com",
    "Desert Bluffs Poker Room":              "desertbluffspoker.com",
    "Fortune Lacey Casino & Poker":          "fortunelacey.com",
    "Macau Casino — Tukwila":                "macaucasinotukwila.com",
    "Oak Harbor Card Room":                  "oakharborcardroom.com",
    "Papa's Sports Lounge & Casino":         "papascasinomoseslake.com",
    "Roxbury Lanes Casino":                  "roxburylanes.com",
    "Slo Pitch Sports Grill & Casino":       "slopitchbellingham.com",
    "All Star Lanes & Casino":               "allstarlanes.com",
    "Mac's Bar & Cardroom":                  "macsbaraberdeen.com",

    # ── WEST VIRGINIA ────────────────────────────────────────────────────────
    "Mardi Gras Casino & Resort":            "mardigrasresort.com",
    "Wheeling Island Hotel-Casino-Racetrack": "wheelingisland.com",

    # ── WISCONSIN ────────────────────────────────────────────────────────────
    "Potawatomi Hotel & Casino":             "paysbig.com",
    "Ho-Chunk Gaming Wisconsin Dells":       "ho-chunkgaming.com",
    "St. Croix Casino Turtle Lake":          "stcroixcasino.com",
    "Menominee Casino Resort":               "menomineecasinoresort.com",

    # ── WYOMING ──────────────────────────────────────────────────────────────
    "Wind River Hotel & Casino":             "windrivercasino.net",
    "Ace High Social Club":                  "acehighlaramie.com",
    "Outlaw Poker":                          "outlawpokerlaramie.com",
}


def name_to_slug(name: str) -> str:
    """Convert a card room name to a hyphenated filename slug.
    E.g. "Commerce Casino" → "commerce-casino"
    """
    slug = name.lower()
    slug = re.sub(r"['''\u2019]", "", slug)          # strip apostrophes
    slug = re.sub(r"[^a-z0-9]+", "-", slug)          # non-alnum → hyphen
    slug = slug.strip("-")
    return slug


def slug_to_resource_name(slug: str) -> str:
    """Convert slug to Android/iOS resource name.
    E.g. "commerce-casino" → "logo_commerce_casino"
    """
    return "logo_" + slug.replace("-", "_")


def asset_exists(resource_name: str) -> bool:
    android_path = os.path.join(ANDROID_DRAWABLE, f"{resource_name}.png")
    return os.path.exists(android_path)


def fetch_clearbit(domain: str):
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


def fetch_google_favicon(domain: str):
    url = f"https://www.google.com/s2/favicons?domain={domain}&sz=128"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=10)
        if resp.status_code == 200 and "image" in resp.headers.get("content-type", ""):
            return resp.content
    except Exception:
        pass
    return None


def is_generic_favicon(image_data: bytes) -> bool:
    """Heuristic: if the image is tiny (≤16 KB) and has very few unique colors
    it might be a generic browser-default icon. We accept it anyway since even
    a favicon-sized logo is better than nothing."""
    return len(image_data) < 200  # only reject extremely tiny blobs (<200 bytes)


def save_logo(image_data: bytes, resource_name: str) -> bool:
    """Resize to LOGO_SIZExLOGO_SIZE, save to Android drawable and iOS imageset."""
    try:
        img = Image.open(BytesIO(image_data)).convert("RGBA")
        img = img.resize((LOGO_SIZE, LOGO_SIZE), Image.LANCZOS)

        # Android
        android_path = os.path.join(ANDROID_DRAWABLE, f"{resource_name}.png")
        img.save(android_path, "PNG")

        # iOS imageset
        ios_dir = os.path.join(IOS_ASSETS, f"{resource_name}.imageset")
        os.makedirs(ios_dir, exist_ok=True)
        ios_png = os.path.join(ios_dir, f"{resource_name}.png")
        img.save(ios_png, "PNG")

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
        print(f"    ✗ Failed to save image: {e}")
        return False


def main():
    with open(ANDROID_JSON) as f:
        rooms = json.load(f)

    missing = [r for r in rooms if not r.get("logo")]
    print(f"Rooms without logos: {len(missing)}")
    print("=" * 60)

    success_count = 0
    skip_count = 0
    fail_count = 0

    for room in missing:
        name = room["name"]
        slug = name_to_slug(name)
        resource_name = slug_to_resource_name(slug)
        logo_filename = f"{slug}.png"

        # Skip if already downloaded in a previous run
        if asset_exists(resource_name):
            print(f"  SKIP (exists): {name}")
            room["logo"] = logo_filename
            skip_count += 1
            continue

        domain = DOMAIN_MAP.get(name)
        if not domain:
            print(f"  SKIP (no domain): {name}")
            fail_count += 1
            continue

        print(f"  {name}  →  {domain}", end=" … ", flush=True)

        image_data = fetch_clearbit(domain)
        source = "Clearbit"

        if image_data is None or is_generic_favicon(image_data):
            image_data = fetch_google_favicon(domain)
            source = "Google"

        if image_data is None or is_generic_favicon(image_data):
            print(f"✗ no image")
            fail_count += 1
            time.sleep(0.3)
            continue

        if save_logo(image_data, resource_name):
            room["logo"] = logo_filename
            print(f"✓  [{source}]")
            success_count += 1
        else:
            fail_count += 1

        time.sleep(0.25)  # be polite to APIs

    print()
    print(f"Results: {success_count} saved, {skip_count} already existed, {fail_count} failed/skipped")

    # Write updated JSON to both locations
    out = json.dumps(rooms, indent=2, ensure_ascii=False)
    with open(ANDROID_JSON, "w") as f:
        f.write(out)
    with open(IOS_JSON, "w") as f:
        f.write(out)

    # Sync ios/Stax/Resources/cardrooms.json if it exists and differs
    ios_resources_json = os.path.join(BASE, "ios/Stax/Resources/cardrooms.json")
    if os.path.exists(ios_resources_json):
        with open(ios_resources_json, "w") as f:
            f.write(out)
        print(f"Synced ios/Stax/Resources/cardrooms.json")

    print("JSON files updated.")


if __name__ == "__main__":
    main()
