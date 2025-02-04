push 0
push 5
push function0
lfp
push 3
push 2
lfp
stm
ltm
ltm
push -3
add
lw
js
print
halt

function0:
cfp
lra
lfp
push 1
add
lw
lfp
push 2
add
lw
add
lfp
lw
push -2
add
lw
add
stm
sra
pop
pop
pop
sfp
ltm
lra
js