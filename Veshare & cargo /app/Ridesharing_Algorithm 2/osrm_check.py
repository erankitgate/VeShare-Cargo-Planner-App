import json
import requests
from pprint import pprint

#url = 'http://0.0.0.0:5000/table/v1/car/'
#url += '2.25975,48.923557;2.262194,48.922554'


with open('input_to_python//filename.txt') as myfile:
    url = list(myfile)[-1]

print("url is", url)
response = requests.get(url.strip())
json_data = json.loads(response.text)
#print(json_data["routes"][0]["duration"])
print(json_data)
listToStr = ' '.join(map(str, json_data))
  
#print(listToStr)
f = open("output_from_python//myfile.txt", "a+")
f.write(str(json_data["routes"][0]["duration"])+"\n")
f.close()
print("python executed")
