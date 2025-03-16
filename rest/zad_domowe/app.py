from fastapi import FastAPI, HTTPException, status, Query, Path, Body
from fastapi.responses import JSONResponse

from typing import Annotated, List, Dict, Optional
from pydantic import BaseModel, Field
from datetime import datetime, timedelta, date
import pytz

from geocoder import *

# Może pokazuj też niewidoczne, żeby było ich więcej w wynikach 

app = FastAPI()

class GetCoordinatesRequest(BaseModel):
    address: str 

@app.post('/v1/coordinates')
async def GetCoordinates(request: GetCoordinatesRequest):
    response = await GetCoordinatesFromAddress(request.address)
    lat = TransformCoordinates(response['lat'])
    lng = TransformCoordinates(response['lng'])

    return {
        'lat': lat,
        'lng': lng    
    }
#

class GetSunsetTimeRequest(BaseModel):
    lattitude: float 
    longitude: float 

@app.post('/v1/sunset')
async def GetSunsetTime(request: GetSunsetTimeRequest):
    today = date.today()
    response = await GetSunsetTimeFromCoordinates(request.lattitude, request.longitude, today)
    return response 
#

class GetVisibleSatellitePassRequest(BaseModel):
    address: str 
    satelliteID: int 

@app.post('/v1/pass')
async def GetVisibleSatellitePass(request: GetVisibleSatellitePassRequest):
    coordinates = await GetCoordinatesFromAddress(request.address)
    lat = TransformCoordinates(coordinates['lat'])
    lng = TransformCoordinates(coordinates['lng'])

    today = date.today()
    sunsets = await GetSunsetTimeFromCoordinates(lat, lng, today)
    
    times = await GetVisibleSatellitePassesFromCoordinates(lat, lng, request.satelliteID)

    endNight = datetime.fromisoformat(sunsets['sunrise'])
    endNight = endNight + timedelta(days=1)
    startNight = datetime.fromisoformat(sunsets['sunset'])

    print(startNight)
    print(endNight)

    times = map(lambda elm: datetime.fromisoformat(elm['rise']['utc_datetime']), times)
    times = filter(lambda elm: startNight <= elm and elm <= endNight, times)
    times = map(lambda elm: changeTimezone(elm), times)
    return list(times)
#

def changeTimezone(time):
    warsawZone = pytz.timezone('Europe/Warsaw')
    return time.astimezone(warsawZone)
#

# https://sat.terrestre.ar/?ref=public_apis&utm_medium=website
# https://sunrise-sunset.org/api?ref=public_apis&utm_medium=website
# https://opencagedata.com/dashboard#geocoding