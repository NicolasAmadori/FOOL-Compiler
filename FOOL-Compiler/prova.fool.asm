push 1
push 0
beq label2
push 1
push 0
beq label2
push 1
b label3
label2:
push 0
label3:
push 0
beq label0
push 0
b label1
label0:
push 1
label1:
print
halt