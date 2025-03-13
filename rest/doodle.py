from fastapi import FastAPI, HTTPException, status, Query, Path, Body
from fastapi.responses import JSONResponse

from typing import Annotated, List, Dict, Optional
from pydantic import BaseModel, Field

app = FastAPI()
next_poll_id = 1

@app.get('/v1/poll/')
async def get_polls():
    return polls 
# end procedure get_polls()

class CreatePollRequest(BaseModel):
    poll_id: Optional[int] = Field(default=None, gt=0)
    options: List[str]  = Field(min_items=2)
    description: str = ""
    title: str 

class Poll(CreatePollRequest):
    votes: dict = {}

polls: Dict[int, Poll] = {}

@app.post('/v1/poll/')
async def create_poll(poll: CreatePollRequest):
    global next_poll_id

    if poll.poll_id is None: 
        poll.poll_id = next_poll_id
        next_poll_id += 1
    #

    new_poll = Poll(**poll.model_dump())
    new_poll.votes = {option: 0 for option in poll.options}

    polls[poll.poll_id] = new_poll
    return JSONResponse(
        status_code=status.HTTP_200_OK,
        content=new_poll.model_dump()
    )
# end procedure create_poll()

@app.post('/v1/poll/{poll_id}/vote')
async def vote_poll(
    poll_id: int,
    option: str = Body(default="", embed=True)
):
    if poll_id not in polls:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="This poll doesn't exists. Create one first!"
        )
    #

    poll = polls[poll_id]
    if option not in poll.options:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"This option doesn't exists. Possible options: {poll.options}"
        )
    #        

    poll.votes[option] += 1 
    return poll
# end procedure vote_poll()        

@app.get('/v1/poll/{poll_id}/votes')
async def results_poll(poll_id: int):
    if poll_id not in polls:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="This poll doesn't exists. Create one first!"
        )
    #

    poll = polls[poll_id]

    return JSONResponse(
        status_code=status.HTTP_200_OK,
        content={
            "poll_id": poll.poll_id,
            "title": poll.title,
            "results": poll.votes
        }
    )
# end procedure results_poll()

class UpdatePollRequest(BaseModel):
    options: Optional[List[str]]  = Field(default=None, min_items=2)
    description: Optional[str] = None
    title: Optional[str] = None 
#

@app.put('/v1/poll/{poll_id}')
async def update_poll(
    poll_id: int,
    request: UpdatePollRequest
):
    if poll_id not in polls:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="This poll doesn't exists. Create one first!"
        )
    #

    poll = polls[poll_id]

    poll.description = request.description if request.description is not None else poll.description
    poll.title = request.title if request.title is not None else poll.title 
    if request.options is not None:
        poll.options = request.options
        poll.votes = {option: 0 for option in request.options}

    return JSONResponse(
        status_code=status.HTTP_200_OK,
        content=poll.model_dump()
    )
# end proceure update_poll()        

@app.delete('/v1/poll/{poll_id}')
async def delete_pool(poll_id: int):
    pass 