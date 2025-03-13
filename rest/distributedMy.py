from fastapi import FastAPI
from enum import Enum 
from typing import Union 

app = FastAPI()

@app.get('/')
async def root():
    return {"message": "Hello, world!"}

@app.get('/items/{item_id}')
async def read_item(item_id: int):
    return {"item_id": item_id}

class ModelName(str, Enum):
    alexnet = 'alexnet'
    resnet  = 'resnet'
    lenet   = 'lenet'

@app.get('/models/{model_name}')
async def get_model(model_name: ModelName):
    if model_name is ModelName.alexnet:
        return {"model_name": model_name, "message": "Deep Learning FTW!"}
    if model_name.value == "lenet":
        return {"model_name": model_name, "message": "LeCNN all the images"}

    return {"model_name": model_name, "message": "Have some residuals"}
#

# fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]
# @app.get('/items/')
# async def read_item(skip: int = 0, limit: Union[int, None] = None):
#     if limit:
#         return fake_items_db[skip : skip + limit]
#     else:
#         return "Proved limit in query parametr"
#

from pydantic import BaseModel 

class Item(BaseModel):
    name: str 
    description: Union[str, None] = None 
    price: float 
    tax: Union[float, None] = None 
#

@app.post("/items/")
async def create_item(item: Item):
    item_dict = item.model_dump()
    if item.tax is not None:
        price_with_tax = item.price + item.tax
        item_dict.update({"price_with_tax": price_with_tax})
    return item_dict