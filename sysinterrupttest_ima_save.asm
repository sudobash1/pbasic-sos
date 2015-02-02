# tests system interrupt calls on invalid memory address
:start
SET R1 0
SET R3 400
SAVE R3 R1
BRANCH start
