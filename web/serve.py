#!/usr/bin/env python
import http.server, sys

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_my_headers()
        http.server.SimpleHTTPRequestHandler.end_headers(self)

    def send_my_headers(self):
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")


if __name__ == '__main__':
    if len(sys.argv) > 1:
        try:
            portArg = int(sys.argv[1])
        except ValueError: pass
    http.server.test(HandlerClass=MyHTTPRequestHandler, port=portArg)