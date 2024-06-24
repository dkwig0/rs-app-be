#/bin/bash

curl --location --request POST 'https://fgx3ytjw71.execute-api.eu-west-1.amazonaws.com/products' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "API Product 2",
    "title": "API Product 2",
    "description": "API Product 2",
    "price": 20,
    "count": 80
}'