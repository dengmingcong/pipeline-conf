import json
import os, sys
import shutil

dirpath = os.path.dirname(__file__)


'''
    思路：
        1、找到当前文件夹下以fullTest开头和CI.json结尾的所有文件，并遍历
        2、提供2个方法：
          -- 方法一：（修改fullTest的）将文件修改后，另存为testonline.json 和 predeploy.json结尾的文件
          -- 方法二：（修改Regression_的）将文件修改后，判断下
             如果是CI的Regression，修改如下：
                 ①：branch修改成Regression_branch的方式
             如果是testonline 或者 predeploy的，修改如下：
                ①：branch修改成：Regression_branch
                ②：testSripts修改成：testScripts
'''


# 获取当前路径下以fullTest开头，以CI.json结尾的文件
def json_file():
    for parent, dirnames, filenames in os.walk(dirpath):
        for filename in filenames:
            if filename.startswith("fullTest") and filename.endswith("CI.json"):
                print(filename)
                new_name = filename.replace('CI.json', 'testonline.json')  # 为文件赋予新名字
                shutil.copyfile(os.path.join(filename), os.path.join(new_name))  # 复制并重命名文件
                print(filename, "copied as", new_name)


def save_as_testonline(src, env):
    """
    1、修改以fullTest开头和CI.json结尾的文件内容；
    2、将1中修改的内容保存为testonline.json 和 predeploy.json格式的文件
    :param src: 源文件
    :param env: 环境
    :return:
    """

    with open(src) as f:
        data = json.load(f)  # 加载json文件中的内容给data
        data['env'] = env
        data['git']['branch'] = 'master'
        print("jmx", data['jmeter']['jmx'])
        jmx = data['jmeter']['jmx']
        jmx2 = jmx.replace("testSripts", "testScripts")
        data['jmeter']['jmx'] = jmx2
        print("替换后的jmx的值", jmx2)
        print("data", data)

    dst = src.replace("CI", env)

    with open(dst, "w") as f:
        json.dump(data, f ,indent=4)


def save_as_regression(src, env):
    """
     1、修改以fullTest开头和CI.json结尾的文件内容；
        --如果env = CI，则将branch分支修改为Regression_xxxx
        --如果env != CI，则将branch分支修改为Regression_master，将testSripts修改成testScripts
    2、如果env != CI，则将修改后的文件重命名为：Regression_xxxxx${env}.json
       如果env == CI，则将修改后的文件重命名为：Regression_xxxxCI.json

    :param src:
    :param env:
    :return:
    """

    with open(src) as f:
        data = json.load(f)  # 加载json文件中的内容给data
        data['env'] = env
        if env.upper() == "CI":
            data['git']['branch'] = "Regression_" + data['git']['branch']
        else:
            data['git']['branch'] = "Regression_master"
            jmx = data['jmeter']['jmx']
            jmx2 = jmx.replace("testSripts", "testScripts")
            data['jmeter']['jmx'] = jmx2
        print("data", data)

    if env.upper() == "CI":
        dst = "Regression_" + src
    else:
        dst = "Regression_" + src.replace("CI", env)

    with open(dst, "w") as f:
        json.dump(data, f, indent=4)





def read_list():
    list = []
    f = open("list.txt")
    lines = f.readlines()

    for line in lines:
        line = line.replace('\n', '')
        line = line + ".json"
        list.append(line)
    return list


def edit_file():
    """
    type:
    :return:
    """
    for parent, dirnames, filenames in os.walk(dirpath):
        for filename in filenames:
            # 获取当前路径下以fullTest开头和CI.json结尾的文件
            if filename.startswith("fullTest") and filename.endswith("CI.json"):
                list = read_list()
                if filename in list:
                    save_as_testonline(filename, "testonline")
                    save_as_testonline(filename, "predeploy")
                    save_as_regression(filename, "ci")
                    save_as_regression(filename, "testonline")
                    save_as_regression(filename, "predeploy")


edit_file()
