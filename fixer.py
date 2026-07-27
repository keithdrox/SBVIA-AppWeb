import os, re
d = 'backend/src/main/java/com/sbvia/backend/entity'
for f in os.listdir(d):
    if f.endswith('.java'):
        p = os.path.join(d, f)
        with open(p, 'r', encoding='utf-8') as file:
            c = file.read()
        c = re.sub(r'name = "(id_[a-zA-Z]+)"', r'name = "\"\\1\""', c)
        with open(p, 'w', encoding='utf-8') as file:
            file.write(c)
print("Done!")
