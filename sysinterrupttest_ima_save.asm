# tests system interrupt for saving outside of memory
SET R1 0

# -1 (relative) is outside the valid memory ranges .
SET R3 -1
SAVE R1 R3
