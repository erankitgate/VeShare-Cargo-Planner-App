import json
import requests
from pprint import pprint

#url = 'http://0.0.0.0:5000/table/v1/car/'
#url += '2.25975,48.923557;2.262194,48.922554'


with open('input_to_python//table_url.txt') as myfile:
    url = list(myfile)[-1]

#print("url is", url)
response = requests.get(url.strip())

json_data = json.loads(response.text)


f = open('output_from_python//distance_matrix.txt', "a")
d = json_data["distances"]

for x in d:
    f.write(str(x))
    f.write("\n")

f.write("\n\n")

#remove comma and [] from file

f.close()
