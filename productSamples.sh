PRODUCTS_TABLE="product"
STOCKS_TABLE="stock"

PRODUCTS=(
    '{"id": {"S": "1"}, "title": {"S": "Product 1"}, "name": {"S": "Product 1"}, "description": {"S": "Product 1"}, "price": {"N": "10"}}'
    '{"id": {"S": "2"}, "title": {"S": "Product 2"}, "name": {"S": "Product 1"}, "description": {"S": "Product 2"}, "price": {"N": "20"}}'
    '{"id": {"S": "3"}, "title": {"S": "Product 3"}, "name": {"S": "Product 3"}, "description": {"S": "Product 3"}, "price": {"N": "20"}}'
    '{"id": {"S": "4"}, "title": {"S": "Product 4"}, "name": {"S": "Product 4"}, "description": {"S": "Product 4"}, "price": {"N": "20"}}'
)

STOCKS=(
    '{"product_id": {"S": "1"}, "count": {"N": "20"}}'
    '{"product_id": {"S": "2"}, "count": {"N": "80"}}'
    '{"product_id": {"S": "3"}, "count": {"N": "1"}}'
    '{"product_id": {"S": "4"}, "count": {"N": "8000"}}'
)

for product in "${PRODUCTS[@]}"; do
    aws dynamodb put-item --table-name $PRODUCTS_TABLE --item "$product"
done

for stock in "${STOCKS[@]}"; do
    aws dynamodb put-item --table-name $STOCKS_TABLE --item "$stock"
done

echo "Tables populated successfully"