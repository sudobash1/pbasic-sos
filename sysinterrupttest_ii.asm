# tests system interrupt calls on invalid instruction
:start
SET R1 0
SET R3 400
SAVE R3 R1 #Overwrite the program at (relative) addr 0
BRANCH start #Go back to addr 0 and attempt to run the mangled instruction
