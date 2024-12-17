from websockets.asyncio.server import serve 
import asyncio 
import datetime

async def ticker(websocket):
    while True:
        await websocket.send("tick! time: " + str(datetime.datetime.now()))
        await asyncio.sleep(1)


async def main():
    async with serve(ticker, "localhost", 4545):
        print("Server running on port 4545")
        await asyncio.get_running_loop().create_future()


if __name__ == '__main__':
    asyncio.run(main())