-- The Computer Language Benchmarks Game
-- http://benchmarksgame.alioth.debian.org/
-- contributed by Jim Roseborough

seq = io.read("*a")
ilen = #seq
seq = seq:gsub('>[^%c]*%c*',''):gsub('%c+','')
clen = #seq

variants = {
   'agggtaaa|tttaccct',
   '[cgt]gggtaaa|tttaccc[acg]',
   'a[act]ggtaaa|tttacc[agt]t',
   'ag[act]gtaaa|tttac[agt]ct',
   'agg[act]taaa|ttta[agt]cct',
   'aggg[acg]aaa|ttt[cgt]ccct',
   'agggt[cgt]aa|tt[acg]accct',
   'agggta[cgt]a|t[acg]taccct',
   'agggtaa[cgt]|[acg]ttaccct',
}

subst = {
   B='(c|g|t)', D='(a|g|t)',   H='(a|c|t)', K='(g|t)',
   M='(a|c)',   N='(a|c|g|t)', R='(a|g)',   S='(c|g)',
   V='(a|c|g)', W='(a|t)',     Y='(c|t)'
}

function countmatches(variant)
   local n = 0
   local counter = function() n = n + 1 return '-' end
   variant:gsub('([^|]+)|?', function(pattern)
      seq:gsub(pattern,counter)
   end)
   return n
end

for i,p in ipairs(variants) do
   io.write(string.format('%s %d\n', p, countmatches(p)))
end

for k,v in pairs(subst) do
   seq = seq:gsub(k,v)
end

io.write(string.format('\n%d\n%d\n%d\n', ilen, clen, #seq))
