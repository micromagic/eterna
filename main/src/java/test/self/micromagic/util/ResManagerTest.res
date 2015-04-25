# Copyright 2015 xinjunli (micromagic@sina.com).
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# 测试用资源

## res1
void test()
{
   int i = 0;
   i++;
}

## res2
测试测试
abcde ${param1} 12345
# temp memo
结束 ${param2}
${param3}

## res3
# 3个#
###test
over

## indent1
public void setXXX()
      throws Exception
{
   if (i > 0)
   {
      i++;
      char c = ' ';
      for (int i = 0; < 0; i--)
      {
         doOther();
      }
   }
   new OtherClass() {
      public int getI()
      {
      }
   }
}

## exp1
   public void setXXX()
         throws Exception
   {
      if (i > 0)
      {
         i++;
         char c = ' ';
         for (int i = 0; < 0; i--)
         {
            doOther();
         }
      }
      new OtherClass() {
         public int getI()
         {
         }
      }
   }

## indent2
public String getXXX()
    throws Exception
{
if (i > 0)
{
i++;
char c = ' ';
for (int i = 0; < 0; i--)
{
doOther();
}
}
new OtherClass() {
public int getI()
{
}
}
}

## exp2
   public String getXXX()
      throws Exception
   {
      if (i > 0)
      {
         i++;
         char c = ' ';
         for (int i = 0; < 0; i--)
         {
            doOther();
         }
      }
      new OtherClass() {
         public int getI()
         {
         }
      }
   }

## PropertyManager.Release.Constructor
public ${thisName}()
{
   try
   {
      System.out.println("TEST1 0:" + TEST1);
      Utility.addFieldPropertyManager(TEST_PROP_NAME1, this.getClass(), "TEST1");
      System.out.println("TEST1 1:" + TEST1);
      Utility.setProperty(TEST_PROP_NAME1, "2");
      System.out.println("TEST1 2:" + TEST1);
   }
   catch (Exception ex)
   {
      System.out.println(ex);
   }
}