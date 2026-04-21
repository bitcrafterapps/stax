# Card Rooms Delta — Missing Venues & States

This document tracks all poker card rooms and poker rooms in the United States that are **not yet in `cardrooms.json`**. It also flags data quality issues in the existing database.

**Type Key:**
- `[T]` = Tribal Casino
- `[C]` = Commercial Casino
- `[P]` = Parimutuel / Racino
- `[S]` = Standalone Card Room / Social Club

---

## Data Quality Issues in Existing Database

Before adding new entries, the following existing entries need review:

| Venue | State | Issue |
|---|---|---|
| San Manuel Casino | California | Rebranded as **Yaamava' Resort & Casino** in 2021 |
| Harrah's New Orleans | Louisiana | Rebranded as **Caesars New Orleans** |
| Seminole Brighton Casino | Florida | Rebuilt/reopened as **Seminole Brighton Bay Hotel Casino** |
| MotorCity Casino Hotel | Michigan | Poker room **permanently closed** August 2024 |
| Mountaineer Casino | West Virginia | Poker room **permanently closed**; remove from active list |
| Resorts World New York City | New York | Does **NOT** have live poker (electronic table games only) |
| Mirage Poker Room | Nevada | Property transitioned to Hard Rock Las Vegas; poker room status changed |
| Planet Hollywood Poker Room | Nevada | Closed January 2026 |

---

## Missing Venues by State

### Arizona
*Currently in DB: Talking Stick Resort, Gila River Wild Horse Pass, Fort McDowell Casino (3 total)*

| Venue | City | Type |
|---|---|---|
| Harrah's Ak-Chin Casino | Maricopa | T |
| Desert Diamond Casino West Valley | Glendale | T |
| Desert Diamond Casino Tucson | Tucson | T |
| Desert Diamond Casino Sahuarita | Sahuarita | T |
| Desert Diamond Casino Why | Why | T |
| Yavapai Casino | Prescott | T |
| Apache Gold Casino Resort | Globe | T |
| Hon-Dah Resort Casino | Pinetop-Lakeside | T |

---

### California
*Currently in DB: ~33 venues. The California Gambling Control Commission lists 79 active licensed card rooms plus many tribal properties — roughly 40+ venues missing.*

**Missing Standalone Card Rooms:**

| Venue | City | Type |
|---|---|---|
| 500 Club Casino | Clovis | S |
| Ace & Vine | Napa | S |
| Bankers Casino | Salinas | S |
| Casino 99 | Chico | S |
| Casino Chico | Chico | S |
| Crystal Casino | Compton | S |
| Diamond Jim's Casino | Bakersfield | S |
| The Aviator Casino | Bakersfield | S |
| Kings Card Club | Stockton | S |
| Lake Elsinore Casino | Lake Elsinore | S |
| Larry Flynt's Lucky Lady Casino | Gardena | S |
| Limelight Card Room | Sacramento | S |
| Livermore Casino | Livermore | S |
| The Nineteenth Hole | Pacheco | S |
| Ocean's Eleven Casino | Oceanside | S |
| Outlaws Card Parlour | San Luis Obispo | S |
| Palace Poker Casino | Livermore | S |
| Parkwest Casino 580 | Livermore | S |
| Player's Casino | Ventura | S |
| Rogelio's Inc. | Sacramento | S |
| Stars Casino | Stockton | S |
| Towers Casino | Nevada City | S |
| Tres Lounge and Casino | Santa Cruz County | S |
| Westlane Card Room | Stockton | S |

**Missing Tribal Casinos:**

| Venue | City | Type |
|---|---|---|
| Yaamava' Resort & Casino | Highland | T (San Manuel — note: this replaces "San Manuel Casino" in DB) |
| Chumash Casino Resort | Santa Ynez | T |
| River Rock Casino | Geyserville | T |
| Black Oak Casino Resort | Tuolumne | T |
| Table Mountain Casino & Bingo | Friant | T |
| Soboba Casino Resort | San Jacinto | T |
| Fantasy Springs Resort Casino | Indio | T |
| Spotlight 29 Casino | Coachella | T |
| Pala Casino Spa & Resort | Pala | T |
| Valley View Casino & Hotel | Valley Center | T |
| Quechan Resort Casino | Winterhaven | T |
| Agua Caliente Casino Cathedral City | Cathedral City | T |
| Agua Caliente Casino Rancho Mirage | Rancho Mirage | T |
| Win-River Resort & Casino | Redding | T |

---

### Colorado
*Currently in DB: Ameristar Black Hawk, Monarch Casino Black Hawk (2 total)*

| Venue | City | Type |
|---|---|---|
| Bally's Black Hawk Casino | Black Hawk | C |
| Horseshoe Black Hawk (WSOP Poker Room) | Black Hawk | C |
| Midnight Rose Hotel & Casino | Cripple Creek | C |

---

### Delaware
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| Delaware Park Casino & Racing | Wilmington | P |

---

### Florida
*Currently in DB: 11 venues. Many parimutuel card rooms and tribal properties missing.*

**Missing Parimutuel Card Rooms:**

| Venue | City | Type |
|---|---|---|
| The Big Easy Casino | Hallandale Beach | P |
| Bonita Springs Poker Room | Bonita Springs | P |
| bestbet St. Augustine | St. Augustine | P |
| Casino Miami Jai-Alai | Miami | P |
| The Casino at Dania Beach | Dania Beach | P |
| Harrah's Pompano Beach | Pompano Beach | P |
| Melbourne Greyhound Park | Melbourne | P |
| One-Eyed Jack's Poker Room | Sarasota | P |
| Orange City Racing & Card Club | Orange City | P |
| Oxford Downs | Summerfield | P |
| Pensacola Greyhound Track & Poker Room | Pensacola | P |
| Calder Casino | Miami Gardens | P |
| Silks at Tampa Bay Downs | Tampa | P |
| Fort Pierce Jai-Alai Poker Room | Fort Pierce | P |
| Ebro Greyhound Park | Ebro | P |

**Missing Tribal:**

| Venue | City | Type |
|---|---|---|
| Seminole Casino Coconut Creek | Coconut Creek | T |
| Seminole Casino Immokalee | Immokalee | T |
| Seminole Classic Casino | Hollywood | T |
| Miccosukee Resort & Gaming Center | Miami | T |
| Wind Creek Miami | Miami | T |

---

### Illinois
*Currently in DB: Rivers Casino Des Plaines, Grand Victoria Casino (2 total)*

| Venue | City | Type |
|---|---|---|
| Hollywood Casino Aurora | Aurora | C |
| Harrah's Joliet Casino & Hotel | Joliet | C |
| Hollywood Casino Joliet | Joliet | C |
| Bally's Quad Cities Casino & Hotel | Rock Island | C |
| Par-A-Dice Hotel Casino | East Peoria | C |
| Hard Rock Casino Rockford | Rockford | C |
| Wind Creek Chicago Southland | East Hazel Crest | C |
| DraftKings at Casino Queen | East St. Louis | C |

---

### Indiana
*Currently in DB: Horseshoe Hammond (1 total)*

| Venue | City | Type |
|---|---|---|
| Caesars Southern Indiana | Elizabeth | C |
| Horseshoe Indianapolis | Shelbyville | C |
| Four Winds South Bend | South Bend | T |
| Hard Rock Casino Northern Indiana | Gary | C |
| Hollywood Casino Lawrenceburg | Lawrenceburg | C |
| Ameristar Casino East Chicago | East Chicago | C |
| Blue Chip Casino Hotel & Spa | Michigan City | C |
| Rising Star Casino Resort | Rising Sun | C |
| Belterra Casino Resort | Florence | C |
| Harrah's Hoosier Park | Anderson | P |
| Bally's Evansville | Evansville | C |
| Terre Haute Casino Resort | Terre Haute | C |
| French Lick Resort Casino | French Lick | C |

---

### Iowa
*Currently in DB: Prairie Meadows, Horseshoe Council Bluffs (2 total)*

| Venue | City | Type |
|---|---|---|
| Grand Falls Casino Resort | Larchwood | C |
| Riverside Casino & Golf Resort | Riverside | C |

---

### Kansas
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| Hollywood Casino at Kansas Speedway | Kansas City | C |

---

### Louisiana
*Currently in DB: Harrah's New Orleans (now Caesars), L'Auberge Baton Rouge, Coushatta Casino Resort, L'Auberge Lake Charles (4 total)*

| Venue | City | Type |
|---|---|---|
| Caesars New Orleans | New Orleans | C (note: this is the rebrand of "Harrah's New Orleans" already in DB) |
| Bally's Shreveport | Shreveport | C |
| Boomtown Casino New Orleans | Harvey | C |
| Golden Nugget Lake Charles | Lake Charles | C |
| Horseshoe Casino Bossier City | Bossier City | C |
| Jena Choctaw Pines Casino | Dry Prong | T |
| Paragon Casino Resort | Marksville | T |

---

### Maine
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| Hollywood Casino Bangor | Bangor | C |

---

### Maryland
*Currently in DB: Live! Casino Maryland, MGM National Harbor, Horseshoe Baltimore (3 total)*

| Venue | City | Type |
|---|---|---|
| Hollywood Casino Perryville | Perryville | C |

---

### Michigan
*Currently in DB: MotorCity Casino (poker room closed — see data quality section), MGM Grand Detroit, FireKeepers Casino (3 total)*

| Venue | City | Type |
|---|---|---|
| Soaring Eagle Casino & Resort | Mt. Pleasant | T |
| Gun Lake Casino | Wayland | T |
| Island Resort & Casino | Harris | T |
| Odawa Casino Resort | Petoskey | T |

---

### Minnesota
*Currently in DB: Canterbury Park, Running Aces Casino (2 total)*

| Venue | City | Type |
|---|---|---|
| Treasure Island Resort & Casino | Welch | T |
| Shooting Star Casino | Mahnomen | T |
| Seven Clans Casino Warroad | Warroad | T |
| Northern Lights Casino | Walker | T |
| Fortune Bay Resort Casino | Tower | T |
| Cedar Lakes Casino Hotel | Cass Lake | T |

---

### Mississippi
*Currently in DB: Beau Rivage, IP Casino, Harrah's Gulf Coast, Hollywood Casino Tunica (4 total)*

**Gulf Coast:**

| Venue | City | Type |
|---|---|---|
| Hard Rock Hotel & Casino Biloxi | Biloxi | C |
| Golden Nugget Biloxi | Biloxi | C |
| Boomtown Casino Biloxi | Biloxi | C |
| Hollywood Casino Gulf Coast | Bay St. Louis | C |

**Tunica Region:**

| Venue | City | Type |
|---|---|---|
| Horseshoe Casino Tunica | Robinsonville | C |
| Gold Strike Casino Resort | Robinsonville | C |

**Other:**

| Venue | City | Type |
|---|---|---|
| Pearl River Resort (Silver Star & Golden Moon) | Choctaw | T |
| Ameristar Casino Vicksburg | Vicksburg | C |

---

### Missouri
*Currently in DB: Isle of Capri Boonville, Ameristar Kansas City, Hollywood Casino St. Louis (3 total)*

| Venue | City | Type |
|---|---|---|
| Ameristar Casino St. Charles | St. Charles | C |
| Horseshoe Casino St. Louis | St. Louis | C |

---

### Montana
*Not currently in DB — New state. Montana uniquely allows poker in licensed liquor establishments.*

| Venue | City | Type |
|---|---|---|
| Shooters Poker Room | Billings | S |
| The Poker Parlor at Gold Dust Casino | Billings | S |
| Queen of Hearts Card Club | Billings | S |
| Bugz's Cardroom | Billings | S |
| Doc & Eddy's Poker Room | Billings | S |
| Red Door Lounge | Billings | S |
| Stockman's Bar & Poker Room | Missoula | S |
| Press Box Casino, Sports Bar & Restaurant | Missoula | S |
| Westside Lanes & Fun Center | Missoula | S |
| Rimrock Lodge Poker Room | Thompson Falls | S |
| Montana Nugget Casino | Kalispell | S |
| 93 Casino & Liquor | Eureka | S |
| Northern Winz Hotel & Casino | Box Elder | T |
| Ike & Susan's Lounge & Casino | Great Falls | S |
| Nickels Gaming Parlour | Helena | S |
| Rialto | Helena | S |
| Miller's Crossing | Helena | S |
| Korner Klub | Bozeman | S |
| The Cat's Paw | Bozeman | S |
| The Golden Zebra | Bozeman | S |
| Palace Bar & Lanes | Laurel | S |
| Rodiron Poker Room | Sidney | S |

---

### Nebraska
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| Grand Island Casino Resort | Grand Island | C |

---

### Nevada
*Currently in DB: 27 venues (Las Vegas Strip, Downtown, Henderson, Reno, Sparks, Lake Tahoe area)*

**Las Vegas / Clark County — Missing:**

| Venue | City | Type |
|---|---|---|
| Boulder Station Casino | Las Vegas | C |
| Santa Fe Station Hotel & Casino | North Las Vegas | C |
| Palace Station Hotel & Casino | Las Vegas | C |
| Suncoast Hotel & Casino | Las Vegas | C |
| Aliante Casino & Hotel | North Las Vegas | C |
| Westgate Las Vegas Resort & Casino | Las Vegas | C |
| M Resort Spa Casino | Henderson | C |

**Laughlin:**

| Venue | City | Type |
|---|---|---|
| Harrah's Laughlin | Laughlin | C |
| Don Laughlin's Riverside Resort Casino | Laughlin | C |
| Aquarius Casino Resort | Laughlin | C |
| Edgewater Hotel & Casino | Laughlin | C |
| Golden Nugget Laughlin | Laughlin | C |

**Reno / Northern Nevada:**

| Venue | City | Type |
|---|---|---|
| Silver Legacy Resort Casino | Reno | C |
| Circus Circus Reno | Reno | C |
| Alamo Casino | Reno | C |
| Wendover Nugget Casino | West Wendover | C |

---

### New Jersey
*Currently in DB: Borgata, Tropicana, Hard Rock AC, Harrah's Resort AC, Ocean Casino Resort (5 total — all Atlantic City)*

**Missing Atlantic City:**

| Venue | City | Type |
|---|---|---|
| Caesars Atlantic City | Atlantic City | C |
| Golden Nugget Atlantic City | Atlantic City | C |
| Resorts Casino Hotel | Atlantic City | C |
| Bally's Atlantic City | Atlantic City | C |

---

### New Mexico
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| Sandia Resort & Casino | Albuquerque | T |
| Isleta Resort & Casino | Albuquerque | T |
| Inn of the Mountain Gods Resort & Casino | Mescalero | T |
| Route 66 Casino Hotel | Albuquerque | T |
| Buffalo Thunder Resort & Casino | Santa Fe | T |

---

### New York
*Currently in DB: Turning Stone Resort Casino, Resorts World New York City (2 total — note: Resorts World NYC has no live poker)*

| Venue | City | Type |
|---|---|---|
| Resorts World Catskills | Monticello | C |
| Rivers Casino & Resort Schenectady | Schenectady | C |
| del Lago Resort & Casino | Tyre | C |
| Tioga Downs Casino Resort | Nichols | C |
| Seneca Niagara Resort & Casino | Niagara Falls | T |
| Seneca Allegany Resort & Casino | Salamanca | T |
| Akwesasne Mohawk Casino Resort | Hogansburg | T |

---

### North Carolina
*Currently in DB: Harrah's Cherokee Casino Resort (1 total)*

| Venue | City | Type |
|---|---|---|
| Harrah's Cherokee Valley River Casino | Murphy | T |

---

### North Dakota
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| 4 Bears Casino & Lodge | New Town | T |
| Spirit Lake Casino & Resort | St. Michael | T |
| Sky Dancer Casino & Resort | Belcourt | T |

---

### Ohio
*Currently in DB: Jack Cleveland Casino, Hollywood Gaming Dayton (2 total)*

| Venue | City | Type |
|---|---|---|
| Hard Rock Casino Cincinnati | Cincinnati | C |
| Hollywood Casino Columbus | Columbus | C |
| Hollywood Casino Toledo | Toledo | C |
| Jack Cincinnati Casino | Cincinnati | C |

---

### Oklahoma
*Currently in DB: WinStar World Casino, Choctaw Casino Durant, Hard Rock Tulsa, Cherokee Casino Tulsa, Riverwind Casino (5 total)*

| Venue | City | Type |
|---|---|---|
| River Spirit Casino Resort | Tulsa | T |
| Cherokee Casino West Siloam Springs | Colcord | T |
| Choctaw Casino Pocola | Pocola | T |
| Downstream Casino Resort | Quapaw | T |
| Grand Casino Hotel & Resort | Shawnee | T |
| Indigo Sky Casino | Wyandotte | T |

---

### Oregon
*Currently in DB: Spirit Mountain Casino, Chinook Winds Casino (2 total)*

**Missing Tribal:**

| Venue | City | Type |
|---|---|---|
| Seven Feathers Casino Resort | Canyonville | T |
| Three Rivers Casino Resort | Florence | T |
| Wildhorse Resort & Casino | Pendleton | T |
| Indian Head Casino | Warm Springs | T |
| Kla-Mo-Ya Casino | Chiloquin | T |

**Portland-Area Social Clubs:**

| Venue | City | Type |
|---|---|---|
| Final Table Poker Club | Portland | S |
| The Game Poker Club | Portland | S |
| Oregon Poker Club — Rialto | Portland | S |
| Oregon Poker Club — Kit Kat Club | Portland | S |
| Oregon Poker Club — Stadiums Sports Bar | Portland | S |

**Eugene / Southern Oregon:**

| Venue | City | Type |
|---|---|---|
| Full House Poker | Eugene | S |
| High Mountain Poker | Eugene | S |
| The Club House | Eugene | S |
| Medford Social Club Poker Room | Medford | S |
| No Look Poker Club | Medford | S |
| Grants Pass Poker Room | Grants Pass | S |

**Central Oregon:**

| Venue | City | Type |
|---|---|---|
| Bend Poker Room | Bend | S |
| The Diamond Poker Club | Albany | S |

---

### Pennsylvania
*Currently in DB: Parx Casino, Rivers Casino Pittsburgh, Rivers Casino Philadelphia (3 total)*

| Venue | City | Type |
|---|---|---|
| Wind Creek Bethlehem | Bethlehem | T |
| Live! Casino Philadelphia | Philadelphia | C |
| Hollywood Casino at the Meadows | Washington | C |
| Mohegan Sun Pocono | Wilkes-Barre | T |
| Hollywood Casino Morgantown | Morgantown | C |
| Valley Forge Casino Resort | King of Prussia | C |
| Mount Airy Casino Resort | Mount Pocono | C |

---

### Rhode Island
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| Bally's Twin River Casino Hotel | Lincoln | C |
| Bally's Tiverton Casino Hotel | Tiverton | C |

---

### South Dakota
*Not currently in DB — New state. Deadwood operates spread-limit poker; no-limit is tournament-only.*

| Venue | City | Type |
|---|---|---|
| Silverado Franklin Historic Hotel & Gaming | Deadwood | C |
| Cadillac Jack's Gaming Resort | Deadwood | C |
| Saloon No. 10 | Deadwood | C |
| Royal River Casino | Flandreau | T |
| Dakota Sioux Casino | Watertown | T |
| Grand River Casino & Resort | Mobridge | T |

---

### Texas
*Currently in DB: 10 venues. Texas has 80+ registered private social clubs. The following is a representative (not exhaustive) list of missing venues.*

**Tribal:**

| Venue | City | Type |
|---|---|---|
| Kickapoo Lucky Eagle Casino Hotel | Eagle Pass | T |

**Missing Private Social Clubs (representative sample — Houston, DFW, Austin, San Antonio):**

| Venue | City | Type |
|---|---|---|
| 101 Poker Club | Houston | S |
| Aggieland Poker Club | Bryan/College Station | S |
| Alamo Card House | San Antonio | S |
| SA Card House | San Antonio | S |
| Ace Card Club | Fort Worth | S |
| Poker House Fort Worth | Fort Worth | S |
| Poker 1 | Dallas | S |
| 4 Suits Social | Dallas | S |
| 5220 Social | Dallas | S |
| Shuffle 512 | Austin | S |
| Cypress Poker Club | Cypress | S |
| Katy Poker | Katy | S |
| Alpha Social Card Club | Houston | S |
| JokerStars Social Club | Houston | S |
| PrymeTyme Poker House | Houston | S |
| VIP Social Club | Houston | S |
| Jokers of Aggieland | College Station | S |

*Note: Texas social clubs operate in a legal gray area; the full list exceeds 80 venues concentrated in Houston (18+), DFW, Austin, and San Antonio.*

---

### Virginia
*Not currently in DB — New state. Commercial casinos legalized in 2020.*

| Venue | City | Type |
|---|---|---|
| Rivers Casino Portsmouth | Portsmouth | C |
| Caesars Virginia | Danville | C |
| Hard Rock Hotel & Casino Bristol | Bristol | C |

*Two more are in development: Norfolk (Boyd Gaming, ~2026) and a Richmond-area location.*

---

### Washington
*Currently in DB: 13 venues. Washington has 30+ card rooms statewide — approximately 20+ missing.*

**Missing Tribal:**

| Venue | City | Type |
|---|---|---|
| 7 Cedars Casino | Sequim | T |
| Legends Casino Hotel | Toppenish | T |
| Little Creek Casino Resort | Shelton | T |
| Northern Quest Resort & Casino | Airway Heights | T |

**Missing Standalone Card Rooms:**

| Venue | City | Type |
|---|---|---|
| Ace's Poker Lakewood | Lakewood | S |
| Ace's Poker Mountlake Terrace | Mountlake Terrace | S |
| Ace's Poker Yakima | Yakima | S |
| All Star Lanes & Casino | Silverdale | S |
| Black Pearl Casino | Spokane | S |
| Buzz Inn Steakhouse Eastmont | Everett | S |
| Buzz Inn Steakhouse Smokey Point | Arlington | S |
| Buzz Inn Steakhouse Snohomish | Snohomish | S |
| Casino Caribbean Kirkland | Kirkland | S |
| Club 48 Poker Room | Yakima | S |
| Desert Bluffs Poker Room | Kennewick | S |
| Fortune Lacey Casino & Poker | Lacey | S |
| Jamestown Saloon | Arlington | S |
| Lancer Lanes | Clarkston | S |
| Lilac Lanes & Casino | Spokane | S |
| Mac's Bar & Cardroom | Aberdeen | S |
| Macau Casino — Tukwila | Tukwila | S |
| Macau Casino — Lakewood | Lakewood | S |
| Oak Harbor Card Room | Oak Harbor | S |
| Papa's Sports Lounge & Casino | Moses Lake | S |
| Roxbury Lanes Casino | Seattle | S |
| Slo Pitch Sports Grill & Casino | Bellingham | S |

---

### West Virginia
*Currently in DB: Hollywood Casino Charles Town, Mountaineer Casino (2 total — note: Mountaineer poker room permanently closed)*

| Venue | City | Type |
|---|---|---|
| Mardi Gras Casino & Resort | Nitro | P |
| Wheeling Island Hotel-Casino-Racetrack | Wheeling | P |

---

### Wisconsin
*Not currently in DB — New state*

| Venue | City | Type |
|---|---|---|
| Potawatomi Hotel & Casino | Milwaukee | T |
| Ho-Chunk Gaming Wisconsin Dells | Baraboo | T |
| St. Croix Casino | Turtle Lake | T |
| Menominee Casino Resort | Keshena | T |

---

### Wyoming
*Not currently in DB — New state. Very limited poker landscape.*

| Venue | City | Type |
|---|---|---|
| Wind River Hotel & Casino | Riverton | T |
| Ace High Social Club | Laramie | S |
| Outlaw Poker | Laramie | S |

---

## States With No Legal Live Poker

The following states currently have no legal live poker rooms spreading traditional Texas Hold'em or Omaha:

| State | Notes |
|---|---|
| Alabama | No legal poker |
| Alaska | No legal poker (extremely limited tribal gaming, no poker) |
| Arkansas | Racinos offer only electronic gaming |
| Georgia | No legal gambling |
| Hawaii | No legal gambling of any kind |
| Idaho | Legal tribal poker was shut down by courts; no active rooms |
| Kentucky | Racinos offer slots/electronic only; no live table poker |
| New Hampshire | No casino poker (only lottery/charity gaming) |
| South Carolina | No legal poker |
| Tennessee | No casino gambling |
| Utah | No legal gambling of any kind |
| Vermont | No legal poker rooms |

---

## Summary

| State | In DB | Estimated Missing | New State? |
|---|---|---|---|
| Arizona | 3 | 8 | No |
| California | ~33 | 35–40+ | No |
| Colorado | 2 | 3 | No |
| Connecticut | 2 | 0 | No |
| Delaware | 0 | 1 | **Yes** |
| Florida | 11 | 20 | No |
| Illinois | 2 | 8 | No |
| Indiana | 1 | 13 | No |
| Iowa | 2 | 2 | No |
| Kansas | 0 | 1 | **Yes** |
| Louisiana | 4 | 7 | No |
| Maine | 0 | 1 | **Yes** |
| Maryland | 3 | 1 | No |
| Massachusetts | 3 | 0 | No |
| Michigan | 3 | 4 | No |
| Minnesota | 2 | 6 | No |
| Mississippi | 4 | 8 | No |
| Missouri | 3 | 2 | No |
| Montana | 0 | 22 | **Yes** |
| Nebraska | 0 | 1 | **Yes** |
| Nevada | 27 | 17 | No |
| New Jersey | 5 | 4 | No |
| New Mexico | 0 | 5 | **Yes** |
| New York | 2 | 7 | No |
| North Carolina | 1 | 1 | No |
| North Dakota | 0 | 3 | **Yes** |
| Ohio | 2 | 4 | No |
| Oklahoma | 5 | 6 | No |
| Oregon | 2 | 14 | No |
| Pennsylvania | 3 | 7 | No |
| Rhode Island | 0 | 2 | **Yes** |
| South Dakota | 0 | 6 | **Yes** |
| Texas | 10 | 65+ | No |
| Virginia | 0 | 3 | **Yes** |
| Washington | 13 | 22 | No |
| West Virginia | 2 | 2 | No |
| Wisconsin | 0 | 4 | **Yes** |
| Wyoming | 0 | 3 | **Yes** |

**Total new states: 12** (Delaware, Kansas, Maine, Montana, Nebraska, New Mexico, North Dakota, Rhode Island, South Dakota, Virginia, Wisconsin, Wyoming)

**Estimated total missing venues: 370–420+** (not counting the 65+ Texas social clubs)

---

*Last updated: April 2026. Sources: California Gambling Control Commission active license list, PokerAtlas, Bravo Poker Live, state gaming control board websites, and direct venue verification.*
