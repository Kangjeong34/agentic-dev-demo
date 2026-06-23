// 무의존 정적 서버 (Node 내장 http). 두 탭이 같은 origin(http://localhost)에서 열려야
// BroadcastChannel 로 통신할 수 있다 → file:// 직접 열기 대신 이 서버로 서빙한다.
//
//   node tools/serve.js        → http://localhost:5173/  (messenger.html)
//   node tools/serve.js 8080   → 포트 지정
//
// "두 유저"는 두 탭으로 연다:  /?me=alice&name=앨리스   /  /?me=bob&name=밥

import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { extname, join, normalize, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = resolve(fileURLToPath(new URL("../src/web", import.meta.url)));
const PORT = Number(process.argv[2]) || 5173;
const INDEX = "messenger.html";

const TYPES = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
};

const server = createServer(async (req, res) => {
  try {
    const url = new URL(req.url, "http://localhost");
    let pathname = decodeURIComponent(url.pathname);
    if (pathname === "/") pathname = "/" + INDEX;

    // 경로 탈출 방지: 정규화 후 ROOT 하위만 허용.
    const filePath = normalize(join(ROOT, pathname));
    if (!filePath.startsWith(ROOT)) {
      res.writeHead(403).end("forbidden");
      return;
    }

    const body = await readFile(filePath);
    res.writeHead(200, { "Content-Type": TYPES[extname(filePath)] || "application/octet-stream" });
    res.end(body);
  } catch {
    res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" }).end("not found");
  }
});

server.listen(PORT, () => {
  console.log(`buddy live → http://localhost:${PORT}/`);
  console.log(`  탭1: http://localhost:${PORT}/?me=alice&name=앨리스`);
  console.log(`  탭2: http://localhost:${PORT}/?me=bob&name=밥`);
});
