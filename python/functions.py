import random
import sys
import os

#creating functions with python

def addNumber (fNum, lNum):
    sumNum = fNum + lNum
    return sumNum

#use function created above sunNum is only used in function
print(addNumber(1,4))

#using sys.stdin.readline we can read input 
print ('What is Your Name?')
name = sys.stdin.readline()
print('Hello',name)