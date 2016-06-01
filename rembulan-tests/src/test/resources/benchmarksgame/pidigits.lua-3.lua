-- The Computer Language Benchmarks Game
-- http://benchmarksgame.alioth.debian.org/
-- contributed by Mike Pall
-- requires LGMP "A GMP package for Lua 5.1"

local g = {}; require"c-gmp"(g, {})
local add, mul, submul = g.mpz_add, g.mpz_mul_si, g.mpz_submul_ui
local mul2x, div, cmp = g.mpz_mul_2exp, g.mpz_fdiv_qr, g.mpz_cmp
local init, get, write = g.mpz_init_set_d, g.mpz_get_d, io.write

local N = tonumber(arg and arg[1]) or 100
local i, n, a, d, t, u = 0, init(1), init(0), init(1), init(0), init(0)
for k=1,1000000 do
  mul2x(n, 1, t) mul(n, k, n) add(a, t, a) mul(a, k+k+1, a) mul(d, k+k+1, d)
  if cmp(a, n) >= 0 then
    mul2x(n, 1, t) add(t, n, t) add(t, a, t) div(t, d, t, u) add(u, n, u)
    if cmp(d, u) > 0 then
      local y = get(t)
      write(y); i = i + 1; if i % 10 == 0 then write("\t:", i, "\n") end
      if i >= N then break end
      submul(a, d, y) mul(a, 10, a) mul(n, 10, n)
    end
  end
end
if i % 10 ~= 0 then write(string.rep(" ", 10 - N % 10), "\t:", N, "\n") end
