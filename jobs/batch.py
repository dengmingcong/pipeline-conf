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
        data['env'] = env.lower()
        data['git']['branch'] = 'master'
        print("jmx", data['jmeter']['jmx'])
        jmx = data['jmeter']['jmx']
        jmx2 = jmx.replace("testSripts", "testScripts")
        data['jmeter']['jmx'] = jmx2
        print("替换后的jmx的值", jmx2)
        print("data", data)

    dst = src.replace("CI", env)

    with open(dst, "w") as f:
        json.dump(data, f, indent=4)


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
        data['env'] = env.lower()
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


def save_as_thirdpart(src, env):
    """
    1、修改以fullTest开头和Testonline.json结尾的文件内容；
    2、将1中修改的内容保存为testonline.json 和 predeploy.json格式的文件
    :param src: 源文件
    :param env: 环境
    :return:
    """

    with open(src) as f:
        data = json.load(f)  # 加载json文件中的内容给data
        data['env'] = env.lower()
        data['git']['branch'] = 'master'
        print("jmx", data['jmeter']['jmx'])
        print("data", data)

    dst = src.replace("Testonline", env)

    with open(dst, "w") as f:
        json.dump(data, f, indent=4)


def save_as_third_regression(src, env):
    """
     第三方回归文件；
        如果env != Testonline，则将修改后的文件重命名为：Regression_xxxxx${env}.json
        如果env == Testonline，则将修改后的文件重命名为：Regression_xxxxTestonline.json

    :param src:
    :param env:
    :return:
    """

    with open(src) as f:
        data = json.load(f)  # 加载json文件中的内容给data
        data['env'] = env.lower()
        data['git']['branch'] = "Regression_master"
        print("data", data)

    if env == "Testonline":
        dst = "Regression_" + src
    else:
        dst = "Regression_" + src.replace("Testonline", env)

    with open(dst, "w") as f:
        json.dump(data, f, indent=4)

def save_as_master_branch(src, branch):
    """
    修改分支为master分支
    :param src:
    :param branch:
    :return:
    """
    with open(src) as f:
        data = json.load(f)  # 加载json文件中的内容给data
        data['git']['branch'] = branch
        print("data", data)

    with open(src, "w") as f:
        json.dump(data, f, indent=4)


# 读取需要批量修改的list列表
def read_list():
    list = []
    f = open("thirdpart.txt")
    lines = f.readlines()

    for line in lines:
        line = line.replace('\n', '')
        line = line + ".json"
        list.append(line)
    # print("个数：", len(list))
    return list


def edit_file():
    """
    type:
    :return:
    """
    for parent, dirnames, filenames in os.walk(dirpath):
        for filename in filenames:
            # 获取当前路径下以fullTest开头和CI.json结尾的文件
            # if filename.startswith("fullTest") and filename.endswith("CI.json"):
            #     if filename.endswith("Testonline.json"):
            list = read_list()
            if filename in list:
                print(filename)
                        # 修改单品的文件
                        # save_as_testonline(filename, "Testonline")
                        # save_as_testonline(filename, "Predeploy")
                        # save_as_regression(filename, "CI")
                        # save_as_regression(filename, "Testonline")
                        # save_as_regression(filename, "Predeploy")

                        # 修改第三方的文件
                        # save_as_third_regression(filename, "Testonline")
                        # save_as_thirdpart(filename, "Predeploy")
                        # save_as_third_regression(filename, "Predeploy")

                # 修改 branch分支为指定分支，如Yukin或者master
                # save_as_master_branch(filename, "master")


def batch_update_filename():
    """
    批量修改当前路径下的文件名
    :return:
    """
    for parent, dirnames, filenames in os.walk(dirpath):
        for filename in filenames:
            # 获取当前路径下以fullTest开头和CI.json结尾的文件
            if filename.endswith("testonline.json"):
                newname = filename.replace('testonline', 'Testonline')
                os.rename(os.path.join(dirpath, filename), os.path.join(dirpath, newname))
                print('success : ' + newname)
            if filename.endswith("predeploy.json"):
                newname = filename.replace("predeploy", "Predeploy")
                os.rename(os.path.join(dirpath, filename), os.path.join(dirpath, newname))
                print('success : ' + newname)


edit_file()
# batch_update_filename()
