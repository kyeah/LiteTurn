from collections import deque
from tornado import websocket
import threading
import time
import tornado
import matplotlib.pyplot as plt 
import matplotlib.animation as animation

trange = range(1000)

class SensorWebSocket(websocket.WebSocketHandler):
    def open(self):
        self.maxLen = maxLen = 1000
        self.t = deque([time.time()]*maxLen)
        self.x = deque([0.0]*maxLen)
        self.y = deque([0.0]*maxLen)
        self.z = deque([0.0]*maxLen)
        self.updating = False
        self.queue = []

        plotRunner = PlotRunner(self)
        plotRunner.start()
        print "WebSocket opened"

    def on_message(self, message):
        m =  message.split()
        x = float(m[0])
        y = float(m[1])
        z = float(m[2])
        t = time.time()
        self.queue.append((self.x, x))
        self.queue.append((self.y, y))
        self.queue.append((self.z, z))
        self.queue.append((self.t, t))

    def on_close(self):
        print "WebSocket closed"

    def addToBuf( self, buf, val ):
        if len(buf) < self.maxLen:
            buf.append(val)
        else:
            buf.popleft()
            buf.append(val)

    def updatePlot( self, frameNum, plt, a1, a2, a3 ):
        for buf, val in self.queue:
            self.addToBuf( buf, val )
            self.queue.pop()
        a1.set_data(trange, self.x)
        a2.set_data(trange, self.y)
        a3.set_data(trange, self.z)

class TornadoRunner( threading.Thread ):
    def run( self ):
        application.listen(8080)
        tornado.ioloop.IOLoop.instance().start()

class PlotRunner( threading.Thread ):

    def __init__( self, sensorSocket ):
        threading.Thread.__init__(self)
        self.sensorSocket = sensorSocket

    def run( self ):
        # set up animation
        fig, ax = plt.subplots(3)
        fig.tight_layout()
        a1, = ax[0].plot([])
        a2, = ax[1].plot([])
        a3, = ax[2].plot([])

        titles = ["X (East) Rotation", "Y (North) Rotation", "Z (Vertical) Rotation"]
        for i in range(len(ax)):
            ax[i].set_title(titles[i])
            ax[i].set_xlim(0, 999)
            ax[i].set_ylim(-1, 1)

        anim = animation.FuncAnimation(fig, self.sensorSocket.updatePlot,
                                       fargs=(plt, a1, a2, a3), 
                                       interval=50)
 
        # show plot
        plt.show()


application = tornado.web.Application([
    (r"/echo", SensorWebSocket),
])

if __name__ == "__main__":
    tornadoRunner = TornadoRunner()
    tornadoRunner.start()
