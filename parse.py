import re

with open('/Users/gocata/.gemini/antigravity/brain/f57f97bd-c510-4919-b6f5-6e16e05b84f2/.system_generated/steps/1048/content.md', 'r') as f:
    content = f.read()

blocks = content.split('<div class="team-img">')[1:]
print(f"Found {len(blocks)} blocks")
speakers = []
for b in blocks:
    img_match = re.search(r'src="(/image/speaker/\d+)"', b)
    picture = img_match.group(1) if img_match else "null"
    
    tw_match = re.search(r'href="https://x.com/([^"]+)"', b)
    twitter = tw_match.group(1) if tw_match else "null"
    if twitter != "null" and twitter.endswith('/"'): twitter = twitter[:-2]
    
    name_match = re.search(r'<h3><a href="/speaker/\d+">([^<]+)</a></h3>', b)
    if name_match:
        name = name_match.group(1).replace('&nbsp;', ' ')
        parts = name.split(' ', 1)
        first = parts[0]
        last = parts[1] if len(parts) > 1 else ""
        
        comp_match = re.search(r'<p>([^<]+)</p>', b)
        company = comp_match.group(1) if comp_match else "JPrime Speaker"
        
        speakers.append((first, last, company, "Expert speaker at JPrime 2026", twitter, f"{first.lower()}@example.com", picture))

for s in speakers:
    tw = f'"{s[4]}"' if s[4] != "null" else "null"
    pic = f'"{s[6]}"' if s[6] != "null" else "null"
    print(f'createSpeaker("{s[0]}", "{s[1]}", "{s[2]}", "{s[3]}", {tw}, "{s[5]}", {pic});')

