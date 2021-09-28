class Cstd
{
  constructor()
  {}

  static memmem(data1, data1Offset, data2)
  {
    for (let i = 0; i <= data1.byteLength - data2.byteLength - data1Offset; ++i)
    {
      let j = 0;
      for (; j != data2.byteLength; ++j)
      {
        if(data1[i + j + data1Offset] != data2[j])
        {
          break;
        }
      }
      if(j >= data2.byteLength)
      {
        return i + data1Offset;
      }
    }
    return -1;
  }

  static memcmp(data1, data1Offset, data2)
  {
    for(let i = 0; i != data2.byteLength; ++i)
    {
      if(data1[i + data1Offset] != data2[i])
      {
        return -1;
      }
    }
    return 0;
  }

  static memcpy(data1, data1Offset, data2, data2Begin, data2End)
  {
    data1.set(data2.subarray(data2Begin, data2End), data1Offset);
  }

  static milliSecondTime()
  {
    return new Date().getTime();
  }

}
