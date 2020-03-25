import random
import sys
import os

#loops

#for loop starting at 0 - 9
for x in range(0,10):
    print (x, ' ')

#newline
print('\n')

#create list and loop through
grocery_list = [' ', 'water', 'potatoes',
             'bannas']
for y in grocery_list:
    print(y)

#loop from 2 - 10
for x in [2,4,6,8,10]:
    print(x)

#loops within loops
num_list =[[1,2,3],[10,20,30],[100,200,300]]

for x in range (0,3):
    for y in range(0,3):
        print(num_list[x][y])

