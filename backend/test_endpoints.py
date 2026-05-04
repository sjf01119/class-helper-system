import urllib.request
import urllib.parse
import json

base_url = "http://localhost:8080/api"

# Login
login_data = json.dumps({
    "username": "student1",
    "password": "123456"
}).encode('utf-8')

req = urllib.request.Request(f"{base_url}/auth/login", data=login_data, headers={'Content-Type': 'application/json'})
try:
    response = urllib.request.urlopen(req)
    res_body = response.read().decode('utf-8')
    print("Login:", response.status, res_body)
    data = json.loads(res_body)
    token = data.get("data", {}).get("token")
except Exception as e:
    print("Login failed", e)
    exit(1)

if not token:
    print("No token")
    exit(1)

headers = {
    "Authorization": f"Bearer {token}"
}

endpoints = [
    "/course/my-student",
    "/assignment/student/list",
    "/resource/list",
    "/user/info",
    "/class/my-student"
]

for ep in endpoints:
    req = urllib.request.Request(f"{base_url}{ep}", headers=headers)
    try:
        r = urllib.request.urlopen(req)
        content = r.read().decode('utf-8')
        print(f"GET {ep}: {r.status} {content[:100].encode('gbk', 'ignore').decode('gbk')}")
    except urllib.error.HTTPError as e:
        print(f"GET {ep}: {e.code} {e.read().decode('utf-8')}")
