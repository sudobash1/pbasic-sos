# 400 (relative) is outside the valid memory ranges 
# for our test program which sets the 'malloc' to
# 300 memory spaces
SET R0 400

# trying to load something from a relative address
# outside what we have allocated, should interrupt
LOAD R3 R0

SET R0 0
