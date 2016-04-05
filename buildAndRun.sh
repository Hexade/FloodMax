mkdir -p bin
javac src/*.java -d bin/
cp input.dat bin/
cd bin/
java Main > output.dat
