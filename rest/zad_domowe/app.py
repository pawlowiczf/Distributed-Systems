from fastapi import FastAPI, HTTPException, status, Query, Path, Body
from fastapi.responses import JSONResponse, HTMLResponse

from typing import Annotated, List, Dict, Optional
from pydantic import BaseModel, Field, EmailStr
from datetime import datetime, timedelta, date
import pytz
import uuid

from geocoder import *

# Może pokazuj też niewidoczne, żeby było ich więcej w wynikach 

app = FastAPI()

reserved = {}
codes = {}
MAX_NUMBER_OF_REQUESTS = 3

@app.get("/", response_class=HTMLResponse)
async def main():
    return """
        <!DOCTYPE html>
        <html lang="pl">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Satellite passes</title>
            <script>
                function sendJSON(event) {
                    event.preventDefault();

                    const submitBtn = document.getElementById("submit-btn");
                    submitBtn.disabled = true;
                    submitBtn.textContent = "Sending...";

                    const address = document.getElementById("address").value;
                    const satelliteID = document.getElementById("satellite-id").value;
                    const method = document.querySelector('input[name="method"]:checked').id;
                    const code = document.getElementById("code").value

                    let url = method === "left" ? "/v1/passes" : "/v1/passes-all";

                    const data = {
                        address: address,
                        satelliteID: satelliteID,
                        uuid: code
                    };

                    fetch(url, {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify(data)
                    })
                    .then(response => {
                        if (!response.ok) {
                            return response.text().then(text => {
                                throw new Error(`HTTP ${response.status}: ${text}`);
                            });
                        }
                        return response.json();
                    })
                    .then(data => {
                        const responseDiv = document.getElementById("response");
                        responseDiv.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
                    })
                    .catch(error => {
                        const responseDiv = document.getElementById("response");
                        responseDiv.innerHTML = `<strong>Error:</strong><br>${error.message}`;
                    })
                    .finally(() => {
                        submitBtn.disabled = false;
                        submitBtn.textContent = "Submit";
                    });
                }
            </script>
        </head>
        <body>
            <h2>Enter your address and pick satellite</h2>
            <form id="satellite-form" onsubmit="sendJSON(event)">
                <label for="address">Address</label>
                <input type="text" id="address" name="address" required>
                <br><br>

                <label for="code">Authorization code</label>
                <input type="text" id="code" name="code" required>
                <br><br>

                <label for="satellite-id">Select satellite</label>
                <select id="satellite-id" name="satellite-id" required>
                    <option value="25544">ISS</option>
                    <option value="43613">ICESAT-2 (pogodowy)</option>
                    <option value="29108">CALIPSO (pogodowy)</option>
                    <option value="54216">CSS MENGTIAN</option>
                </select>
                <br><br>

                <label>
                    <input type="radio" name="method" id="left" value="submit1">
                    Only visible passes
                </label>
                <br>
                <label>
                    <input type="radio" name="method" id="right" value="submit2" checked>
                    All passes
                </label>
                <br><br>

                <button id="submit-btn" type="submit">Submit</button>
            </form>
            
            <div id="response" style="margin-top: 20px;"></div>
        </body>
        </html>
    """
#

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

class GetVisibleSatellitePassesRequest(BaseModel):
    address: str 
    satelliteID: int 
    uuid: str

@app.post('/v1/passes')
async def GetVisibleSatellitePass(request: GetVisibleSatellitePassesRequest):
    if request.uuid not in codes:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Provided authorization code is invalid!"
        )
    #
    if codes[request.uuid] > MAX_NUMBER_OF_REQUESTS:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="You have used all free requests. Generate new code!"
        )
    #
    codes[request.uuid] += 1

    coordinates = await GetCoordinatesFromAddress(request.address)
    lat = TransformCoordinates(coordinates['lat'])
    lng = TransformCoordinates(coordinates['lng'])

    today = date.today()
    sunsets = await GetSunsetTimeFromCoordinates(lat, lng, today)
    
    times = await GetVisibleSatellitePassesFromCoordinates(lat, lng, request.satelliteID)

    endNight = datetime.fromisoformat(sunsets['sunrise'])
    endNight = endNight + timedelta(days=1)
    startNight = datetime.fromisoformat(sunsets['sunset'])

    times = map(lambda elm: datetime.fromisoformat(elm['rise']['utc_datetime']), times)
    times = filter(lambda elm: startNight <= elm and elm <= endNight, times)
    times = map(lambda elm: changeTimezone(elm), times)
    return list(times)
#

@app.post('/v1/passes-all')
async def GetAllSatellitePass(request: GetVisibleSatellitePassesRequest):
    if request.uuid not in codes:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Provided authorization code is invalid!"
        )
    #
    if codes[request.uuid] > MAX_NUMBER_OF_REQUESTS:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="You have used all free requests. Generate new code!"
        )
    #
    codes[request.uuid] += 1

    coordinates = await GetCoordinatesFromAddress(request.address)
    lat = TransformCoordinates(coordinates['lat'])
    lng = TransformCoordinates(coordinates['lng'])

    today = date.today()
    sunsets = await GetSunsetTimeFromCoordinates(lat, lng, today)
    
    times = await GetSatellitePassesFromCoordinates(lat, lng, request.satelliteID)

    endNight = datetime.fromisoformat(sunsets['sunrise'])
    endNight = endNight + timedelta(days=1)
    startNight = datetime.fromisoformat(sunsets['sunset'])

    times = map(lambda elm: datetime.fromisoformat(elm['rise']['utc_datetime']), times)
    times = filter(lambda elm: startNight <= elm and elm <= endNight, times)
    times = map(lambda elm: changeTimezone(elm), times)
    return list(times)
#

class GetCodeRequest(BaseModel):
    email: EmailStr
# 

@app.post('/v1/get-code')
async def GetCode(request: GetCodeRequest):
    if request.email in reserved:
        if datetime.now() >= reserved[request.email]:
            reserved[request.email] = datetime.now() + timedelta(minutes=1)
        else:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"You cannot get new code until {reserved[request.email]}"
            )
    else:
        reserved[request.email] = datetime.now() + timedelta(minutes=1)

    access_code = str(uuid.uuid4())
    codes[access_code] = 0
    
    return {
        'access_code': access_code
    }
    
def changeTimezone(time):
    warsawZone = pytz.timezone('Europe/Warsaw')
    return time.astimezone(warsawZone)
#

# https://sat.terrestre.ar/?ref=public_apis&utm_medium=website
# https://sunrise-sunset.org/api?ref=public_apis&utm_medium=website
# https://opencagedata.com/dashboard#geocoding