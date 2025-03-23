import httpx, re
from fastapi import FastAPI, HTTPException, status, Query, Path, Body

GeocoderKey = "57c1456c8e02494596ded881398171de"


async def GetCoordinatesFromAddress(address: str) -> dict:
    async with httpx.AsyncClient() as client:
        url = f"https://api.opencagedata.com/geocode/v1/json?q={address}&key={GeocoderKey}"
        
        try:
            response = await client.get(url)

            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=f"Server response error: {response.text}")

            try:
                data = response.json() 
            except ValueError:
                raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Bad response. Couldn't process JSON response.")

            if len(data.get('results')) == 0:
                raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Provided address was not found")
            
            return data['results'][0]['annotations']['DMS']
        
        except httpx.RequestError as e:
            raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Connection API error: {str(e)}")
    #
# end procedure GetCoordinatesFromAddress()

async def GetSunsetTimeFromCoordinates(lat: str, lng: str, date: str):
    async with httpx.AsyncClient() as client:
        url = f"https://api.sunrise-sunset.org/json?lat={lat}&lng={lng}&date={date}&formatted=0"
        
        try:
            response = await client.get(url)

            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=f"Server response error: {response.text}")

            try:
                data = response.json() 
            except ValueError:
                raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Bad response. Couldn't process JSON response.")

            if data.get('status') != "OK":
                raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Server response error: {data.get('status')}")
            
            return {
                'sunrise': data['results'].get('sunrise'),
                'sunset': data['results'].get('sunset')
            }
        
        except httpx.RequestError as e:
            raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Connection API error: {str(e)}")
    #    
# end procedure GetSunsetTime()

async def GetVisibleSatellitePassesFromCoordinates(lat: str, lng: str, satelliteID: int):
    async with httpx.AsyncClient() as client:
        url = f"https://sat.terrestre.ar/passes/{satelliteID}?lat={lat}&lon={lng}&days={1}&visible_only=true"
        
        try:
            response = await client.get(url)

            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=f"Server response error: {response.text}")

            try:
                data = response.json() 
            except ValueError:
                raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Bad response. Couldn't process JSON response.")

            return data 
        
        except httpx.RequestError as e:
            raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Connection API error: {str(e)}")
    #        
# end procedure GetVisibleSatellitePassesFromCoordinates()

async def GetSatellitePassesFromCoordinates(lat: str, lng: str, satelliteID: int):
    async with httpx.AsyncClient() as client:
        url = f"https://sat.terrestre.ar/passes/{satelliteID}?lat={lat}&lon={lng}&days={1}"
        
        try:
            response = await client.get(url)

            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail=f"Server response error: {response.text}")

            try:
                data = response.json() 
            except ValueError:
                raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Bad response. Couldn't process JSON response.")

            return data 
        
        except httpx.RequestError as e:
            raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Connection API error: {str(e)}")
    #      
# end procedure GetSatellitePassesFromCoordinates

def TransformCoordinates(coordinates: str):
    pattern = r"(\d+)° (\d+)' ([\d\.]+)'' ([NSEW])"
    match = re.match(pattern, coordinates)

    if not match: return None 

    degrees = int(match.group(1))
    minutes = int(match.group(2))
    seconds = float(match.group(3))
    direction = match.group(4)
    
    decimalValue = degrees + (minutes / 60) + (seconds / 3600)
    
    if direction in ['S', 'W']:
        decimalValue *= -1
    
    return decimalValue
# end procedure TransformCoordinates()

