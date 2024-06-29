#/bin/bash

curl --location --request POST 'https://fgx3ytjw71.execute-api.eu-west-1.amazonaws.com/products' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "API Product 1",
    "title": "API Product 1",
    "description": "API Product 1",
    "price": 20,
    "count": 80
}'