import asyncio
import time
import uuid
from typing import List, Optional
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Todo API")

class User(BaseModel):
    id: str
    username: str
    avatarUrl: str

class Task(BaseModel):
    id: str
    title: str
    description: Optional[str] = None
    isCompleted: bool = False
    priority: Optional[str] = None
    dueDate: Optional[int] = None

class DeleteRequest(BaseModel):
    id: str

# In-memory mock data
tasks_db: List[Task] = [
    Task(
        id=str(uuid.uuid4()),
        title="Doctor appointment",
        description="Annual physical checkup",
        isCompleted=False,
        priority="High",
        dueDate=int(time.time() * 1000) + 86400000 # Tomorrow
    ),
    Task(
        id=str(uuid.uuid4()),
        title="Buy groceries",
        description="Milk, Eggs, Bread",
        isCompleted=False,
        priority="Medium",
        dueDate=None
    ),
    Task(
        id=str(uuid.uuid4()),
        title="Clean the garage",
        description=None,
        isCompleted=False,
        priority=None,
        dueDate=None
    ),
    Task(
        id=str(uuid.uuid4()),
        title="Read a book",
        description="Finish chapter 3",
        isCompleted=False,
        priority="Low",
        dueDate=None
    ),
    Task(
        id=str(uuid.uuid4()),
        title="Call mom",
        description="Wish her happy birthday",
        isCompleted=False,
        priority=None,
        dueDate=None
    )
]

@app.post("/api/user/profile", response_model=User)
async def get_user_profile():
    # Artificially delay by 1 second to make sequential vs parallel performance differences obvious
    await asyncio.sleep(1.0)
    return User(
        id="user-123",
        username="Zhu Tao",
        avatarUrl="https://ui-avatars.com/api/?name=Zhu+Tao&background=0D8ABC&color=fff"
    )

@app.post("/api/tasks/list", response_model=List[Task])
async def list_tasks():
    # Artificially delay by 1 second to make sequential vs parallel performance differences obvious
    await asyncio.sleep(1.0)
    return tasks_db

@app.post("/api/tasks/add", response_model=Task)
async def add_task(task: Task):
    # If the client sends a task without an ID or wants to let server decide,
    # usually we'd generate one. But the client model already has an id.
    tasks_db.append(task)
    return task

@app.post("/api/tasks/update", response_model=Task)
async def update_task(updated_task: Task):
    for idx, task in enumerate(tasks_db):
        if task.id == updated_task.id:
            tasks_db[idx] = updated_task
            return updated_task
    raise HTTPException(status_code=404, detail="Task not found")

@app.post("/api/tasks/delete")
async def delete_task(req: DeleteRequest):
    global tasks_db
    tasks_db = [t for t in tasks_db if t.id != req.id]
    return {"success": True, "id": req.id}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)
