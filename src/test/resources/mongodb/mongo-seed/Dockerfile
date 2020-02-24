FROM mongo
COPY init.json /init.json
CMD mongoimport --host mongodb --db test --collection activities --type json --file /init.json --jsonArray