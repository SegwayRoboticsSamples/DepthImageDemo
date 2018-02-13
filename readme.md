1. getLong() returns 64-bit precision long, bytes/8 helps to eliminate loops

~~~java
for (int i = 0; i < bytes / 8; i++) {
    long l = in.getLong();
    -------
}
~~~
2. depth buffer is 16-bit precision, 64-bit long contains 4 16-bit short segments
~~~java
short [] segments = new short[4];
~~~
3.  swap the first 8 bits and the last 8 bits of each 16-bit short in 64-bit long
~~~java
 long l1 = l & 0x00ff00ff00ff00ffL;
 l1 = l1 << 8;
 long l2 = l & 0xff00ff00ff00ff00L;
 l2 = l2 >> 8 & 0x00ffffffffffffffL;
 l = l1 | l2;
~~~
4. get each 16-bit short from 64-bit long (each value of the array segments is the depth distance )
~~~java
 segments[3] = (short) (0x000000000000ffff & l);
 segments[2] = (short) (l >> 16 & 0x000000000000ffff);
 segments[1] = (short) (l >> 16 & 0x000000000000ffff);
 segments[0] = (short) (l >> 16 & 0x000000000000ffff);
~~~
5.  valid values (500mm -- 1000mm)
~~~java
 for(int j= 0; j<segments.length;j++){
        if (segments[j]<0){
           segments[j] = 0;
        }else if (segments[j]>500 && segments[j]<1000){
            //inside the interval
            segments[j] = (short) ((segments[j]-750)/4);  
        }else{
            segments[j] = 0;
        }
        out.putShort(segments[j]);
  }
~~~
6. trick for better bitmap display though it is not good enough
~~~java
segments[j] = (short) ((segments[j]-750)/4);
~~~
