import sys
from subprocess import call

# print('Number of arguments:', len(sys.argv), 'arguments.')
# print('Argument List:', str(sys.argv))

sts = call('java -jar "SuperCC.jar" '
           + sys.argv[2] + ' ' + sys.argv[3], shell=True)
           # CCEdit passes the arguments in the format "-pr datFile levelNumber"
           # of which we only need the file and level number which subtracting the useless first argument python
           # passes and "-pr" is arguments 2 and 3
           #As SuCC only uses the levelset file and level number that's all we have to pass to it

#I recommend using pyinstaller with the flag --onefile to compile this as its the easiest to manage and use as a CCEdit forwarder