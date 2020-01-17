#!/usr/bin/python
#coding=utf8
import math
import os
import shutil
import sqlite3
import time
from datetime import datetime
from pathlib import Path

import cv2
import numpy as np
import tensorflow as tf

import logging
import numpy as np
import cv2
import os
import cv2 as cv
import numpy as np
__SHOW_PROCESS = False

__model_input_height = 256
__model_input_width = __model_input_height
__model_input_depth = 3
__model_input_layer_name = 'hed_input:0'
__model_is_training_name = 'is_training:0'
__model_output_layer_name = 'hed/dsn_fuse/conv2d/BiasAdd:0'

__HoughLinesPThreshold = 2
__HoughLinesPMinLinLength = __model_input_height / 6.0
__HoughLinesPMaxLineGap = __model_input_height / 25.0

#__min_distance_to_merge = __model_input_height / 10
__min_distance_to_merge = 6
__min_angle_to_merge = 10

class linedetector:
    def __init__(self):
        self.lines = []
 
    def find_lines(self, frame,filrname):
        h, w, ch = frame.shape
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        ret, binary = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)
        #cv2.imshow("binary image", binary)
        cv2.imwrite("binary.png", binary)
        dist = cv2.distanceTransform(binary, cv2.DIST_L1, cv2.DIST_MASK_PRECISE)
        #cv2.imshow("distance", dist / 15)
        dist = dist / 15
        result = np.zeros((h, w), dtype=np.uint8)
        ypts = []
        for row in range(h):
            cx = 0
            cy = 0
            max_d = 0
            for col in range(w):
                d = dist[row][col]
                if d > max_d:
                    max_d = d
                    cx = col
                    cy = row
            result[cy][cx] = 255
            ypts.append([cx, cy])
 
        xpts = []
        for col in range(w):
            cx = 0
            cy = 0
            max_d = 0
            for row in range(h):
                d = dist[row][col]
                if d > max_d:
                    max_d = d
                    cx = col
                    cy = row
            result[cy][cx] = 255
            xpts.append([cx, cy])
 
        #cv2.imshow("lines", result)
        cv2.imwrite("test_image_result_skeleton/"+filrname, result)
 
        frame = self.line_fitness(ypts, image=frame)
        frame = self.line_fitness(xpts, image=frame, color=(255, 0, 0))
 
        #cv2.imshow("fit-lines", frame)
        cv2.imwrite("fitlines.png", frame)
        return self.lines
 
    def line_fitness(self, pts, image, color=(0, 0, 255)):
        h, w, ch = image.shape
        [vx, vy, x, y] = cv2.fitLine(np.array(pts), cv2.DIST_L1, 0, 0.01, 0.01)
        y1 = int((-x * vy / vx) + y)
        y2 = int(((w - x) * vy / vx) + y)
        cv2.line(image, (w - 1, y2), (0, y1), color, 2)
        return image
 
# 计算线段的角度
def __line_degress(line):
    orientation = math.atan2((line[1] - line[3]), (line[0] - line[2]))
    degress = abs(math.degrees(orientation))
    return degress


# 两个向量的夹角
def __two_vector_angle(center, m, n):
    theta = math.atan2(m[0] - center[0], m[1] - center[1]) - math.atan2(n[0] - center[0], n[1] - center[1])

    if theta > math.pi:
        theta -= 2 * math.pi

    if theta < -math.pi:
        theta += 2 * math.pi

    return abs(theta * 180.0 / math.pi)


# 计算两条线段之间的距离
def __line_distance(line1, line2):
    def __line_magnitude(x1, y1, x2, y2):
        lineMagnitude = math.sqrt(math.pow((x2 - x1), 2) + math.pow((y2 - y1), 2))
        return lineMagnitude

    def __point_to_line_distance(point, line):
        px, py = point
        x1, y1, x2, y2 = line
        line_magnitude = __line_magnitude(x1, y1, x2, y2)
        if line_magnitude < 0.00000001:
            return 9999
        else:
            u1 = (((px - x1) * (x2 - x1)) + ((py - y1) * (y2 - y1)))
            u = u1 / (line_magnitude * line_magnitude)

            if (u < 0.00001) or (u > 1):
                # // closest point does not fall within the line segment, take the shorter distance
                # // to an endpoint
                ix = __line_magnitude(px, py, x1, y1)
                iy = __line_magnitude(px, py, x2, y2)
                if ix > iy:
                    distance = iy
                else:
                    distance = ix
            else:
                # Intersecting point is on the line, use the formula
                ix = x1 + u * (x2 - x1)
                iy = y1 + u * (y2 - y1)
                distance = __line_magnitude(px, py, ix, iy)
            return distance

    dist1 = __point_to_line_distance((line1[0], line1[1]), line2)
    dist2 = __point_to_line_distance((line1[2], line1[3]), line2)
    dist3 = __point_to_line_distance((line2[0], line2[1]), line1)
    dist4 = __point_to_line_distance((line2[2], line2[3]), line1)

    return min(dist1, dist2, dist3, dist4)


# 合并同一个分组里面的线段
def __merge_lines_segments(lines_group):
    if (len(lines_group) == 1):
        return lines_group[0]

    points = []
    for x1, y1, x2, y2 in lines_group:
        points.append((x1, y1))
        points.append((x2, y2))

    degress = __line_degress(lines_group[0])
    if (degress > 45) and abs(degress < (90 + 45)):
        # 竖线使用y轴坐标, 最上面和最下面的点
        points = sorted(points, key=lambda point: point[1])
    else:
        # 横线处理x轴坐标, 最左边和最右边的点
        points = sorted(points, key=lambda point: point[0])

    return (*points[0], *points[-1])


# 合并霍夫曼线段
def __merge_hough_lines(lines):
    # 按倾斜角度对线段进行第一次分组(横线，竖线)
    lines_x = []
    lines_y = []
    for l in lines:
        degress = __line_degress(l)
        if (degress > 45) and abs(degress < (90 + 45)):
            lines_x.append(l)
        else:
            lines_y.append(l)

    # 按照线段之间的角度和距离进行合并操作
    super_lines = []

    # 按距离和角度进行第二次分组
    for lines_group in [lines_x, lines_y]:
        for idx, line in enumerate(lines_group):
            group_updated = False
            # 查看当前线段是否可以合并到已有的分组里面
            for group in super_lines:
                for line2 in group:
                    if __line_distance(line2, line) < __min_distance_to_merge:
                        degress_i = __line_degress(line)
                        degress_j = __line_degress(line2)

                        if int(abs(degress_i - degress_j)) < __min_angle_to_merge:
                            group.append(line)

                            group_updated = True
                            break

                if group_updated:
                    break

            # 对于无法插入已有分组的线条建立新分组, 并筛选已经处理过的线段
            if not group_updated:
                new_group = []
                new_group.append(line)

                for line2 in lines_group[idx + 1:]:
                    if __line_distance(line2, line) < __min_distance_to_merge:
                        degress_i = __line_degress(line)
                        degress_j = __line_degress(line2)

                        if int(abs(degress_i - degress_j)) < __min_angle_to_merge:
                            new_group.append(line)

                super_lines.append(new_group)

    # 对分组的线段进行合并
    final_lines = []

    for lines_group in super_lines:
        final_lines.append(__merge_lines_segments(lines_group))

    return final_lines


# 两条线的交点
def __line_intersection(line1, line2):
    xdiff = (line1[0][0] - line1[1][0], line2[0][0] - line2[1][0])
    ydiff = (line1[0][1] - line1[1][1], line2[0][1] - line2[1][1])

    def det(a, b):
        return a[0] * b[1] - a[1] * b[0]

    div = det(xdiff, ydiff)
    if div == 0:
        return None

    d = (det(*line1), det(*line2))
    x = det(d, xdiff) / div
    y = det(d, ydiff) / div
    return (int(x), int(y))


# 查找矩形
def __find_rect(lines):
    # 线段分组
    lines_x = []  # 竖线
    lines_y = []  # 横线
    for l in lines:
        degress = __line_degress(l)
        if (degress > 45) and abs(degress < (90 + 45)):
            lines_x.append(((l[0], l[1]), (l[2], l[3])))
        else:
            lines_y.append(((l[0], l[1]), (l[2], l[3])))

    lines_x = sorted(lines_x, key=lambda x: (x[0][0] + x[1][0]) / 2)  # 从左往右排序
    lines_y = sorted(lines_y, key=lambda x: (x[0][1] + x[1][1]) / 2)  # 从上往下排序

    if len(lines_x) < 2 or len(lines_y) < 2:
        return None

    top_left_point = __line_intersection(lines_x[0], lines_y[0])
    top_right_point = __line_intersection(lines_x[-1], lines_y[0])
    bottom_right_point = __line_intersection(lines_x[-1], lines_y[-1])
    bottom_left_point = __line_intersection(lines_x[0], lines_y[-1])

    return (top_left_point, top_right_point, bottom_right_point, bottom_left_point)



def __load_tf_session(pbfile):
    global tf_session
    if tf_session:
        return tf_session

    # 加载模型
    detection_graph = tf.Graph()
    with detection_graph.as_default():
        od_graph_def = tf.GraphDef()
        with tf.gfile.GFile(pbfile, 'rb') as fid:
            serialized_graph = fid.read()
            od_graph_def.ParseFromString(serialized_graph)
            tf.import_graph_def(od_graph_def, name='')


            tfconfig = tf.ConfigProto(allow_soft_placement=False, log_device_placement=False)
            tfconfig.gpu_options.allow_growth = True
            tf_session = tf.Session(config=tfconfig, graph=detection_graph)

    return tf_session



def hed_edge_detect2(origin_img, model_path):
    try:
        if isinstance(origin_img, str):
            origin_img = cv2.imread(origin_img)

        x_ratio = float(origin_img.shape[1]) / __model_input_width
        y_ratio = float(origin_img.shape[0]) / __model_input_height

        img = cv2.resize(origin_img, (__model_input_width, __model_input_height), 0, 0, cv2.INTER_LINEAR)
        input_img = np.float32(img) / 255.0
        
        # 网络处理
        tf_session = __load_tf_session(model_path)
        output_dict = tf_session.run([__model_output_layer_name], feed_dict={
            __model_input_layer_name: [input_img],
            __model_is_training_name: False
        })
        edge_img = output_dict[0].squeeze()

        edge_img = np.where(edge_img > 0.0, 255, 0).astype(np.uint8)

        if __SHOW_PROCESS:
            cv2.imshow('hed_edge_img', edge_img)

        # 霍夫曼找线段
        lines = cv2.HoughLinesP(edge_img, 1, np.pi * 1 / 180, __HoughLinesPThreshold, __HoughLinesPMinLinLength,
                                __HoughLinesPMaxLineGap)
        print(len(lines))
        lines = lines.squeeze()

        hough_img = edge_img.copy()
        for l in lines:
            x1, y1, x2, y2 = l
            cv2.line(hough_img, (x1, y1), (x2, y2), (0, 0, 255), 1)

        if __SHOW_PROCESS:
            show_img = origin_img.copy()
            for l in lines:
                x1, y1, x2, y2 = l
                x1, y1, x2, y2 = int(x1 * x_ratio), int(y1 * y_ratio), int(x2 * x_ratio), int(y2 * y_ratio)
                cv2.line(show_img, (x1, y1), (x2, y2), (0, 0, 255), 1)
            cv2.imshow('HoughLinesP', show_img)

        # 合并线段
        lines = __merge_hough_lines(lines)

        if __SHOW_PROCESS:
            show_img = origin_img.copy()
            for l in lines:
                x1, y1, x2, y2 = l
                x1, y1, x2, y2 = int(x1 * x_ratio), int(y1 * y_ratio), int(x2 * x_ratio), int(y2 * y_ratio)
                cv2.line(show_img, (x1, y1), (x2, y2), (0, 0, 255), 1)
            cv2.imshow('__merge_hough_lines', show_img)

        # 查找矩形
        rect_points = __find_rect(lines)
        if rect_points == None:
            return (None, edge_img, hough_img)

        rect_points = np.array(rect_points)
        rect_points[:, 0] = rect_points[:, 0] * x_ratio
        rect_points[:, 1] = rect_points[:, 1] * y_ratio
        rect_points.astype(np.int)

        if __SHOW_PROCESS:
            show_img = origin_img.copy()
            for (x1, y1), (x2, y2) in [(rect_points[0], rect_points[1]),
                                       (rect_points[1], rect_points[2]),
                                       (rect_points[2], rect_points[3]),
                                       (rect_points[3], rect_points[0]),
                                       ]:
                cv2.line(show_img, (x1, y1), (x2, y2), (0, 0, 255), 2)

            cv2.imshow('hed_rect', show_img)

        if __SHOW_PROCESS:
            cv2.waitKey(0)

        # 面积过滤
        cnt_area_size = cv2.contourArea(rect_points)
        total_area_size = origin_img.shape[0] * origin_img.shape[1]

        if cnt_area_size / total_area_size < 0.3 or cnt_area_size / total_area_size > 0.99:
  
            return (None, edge_img, hough_img)

        # 四边形的四个内角大小过滤
        angle1 = __two_vector_angle(rect_points[0], rect_points[1], rect_points[3])
        angle2 = __two_vector_angle(rect_points[1], rect_points[2], rect_points[0])
        angle3 = __two_vector_angle(rect_points[2], rect_points[3], rect_points[1])
        angle4 = __two_vector_angle(rect_points[3], rect_points[0], rect_points[2])

        min_angle = min([angle1, angle2, angle3, angle4])
        max_angle = max([angle1, angle2, angle3, angle4])

        if min_angle <= 60 or max_angle >= 120:
            # print('四边形的四个内角大小过滤失败!!!')
            return (None, edge_img, hough_img)

        return (rect_points, edge_img, hough_img)
    except:
        logging.exception('hed_edge_detect exception!!!')

    return (None,None,None)

def img_straighten(image_origin, src_vertices, dst_xsize = 675, dst_ysize = 425):
    rect_dist = [(0, 0), (0, dst_ysize), (dst_xsize, 0), (dst_xsize, dst_ysize)]
    src = [src_vertices[0], src_vertices[3], src_vertices[1], src_vertices[2]]

    m1 = cv2.getPerspectiveTransform(np.float32(src), np.float32(rect_dist))
    img_new = cv2.warpPerspective(image_origin, m1, (dst_xsize, dst_ysize))
    return img_new

def distance_between_point(p1, p2):
    return math.sqrt(pow(p1[0] - p2[0], 2) + pow(p1[1] - p2[1], 2))

def line_detect_possible(image,files):
    widths,heights,pxshu=image.shape
    gray = cv.cvtColor(image, cv.COLOR_BGR2GRAY)
    edges = cv.Canny(gray, 50, 100, apertureSize = 3)
    #cv.imshow("edges", edges)
    
    lines = cv.HoughLinesP(edges, 1, np.pi/180, 7, 100, minLineLength = 3, maxLineGap =20)
    lines = lines.squeeze()
   
    # 合并线段
    lines = __merge_hough_lines(lines)
   
    linexs=[]
    lineys=[]
    maxx=[]
    maxxvalue=0
    maxyvalue=0
    secondmaxx=[]
    maxy=[]
    distancex=[]
    distancey=[]
    for l in lines:
        degress = __line_degress(l)
        if (degress > 45) and abs(degress < (90 + 45)):
            lineys.append(l)
        else:
            linexs.append(l)
    secondmaxx.append(linexs[len(linexs)-1])
    maxx.append(linexs[0])
   
    for line in linexs:
         x1, y1, x2, y2 = line
         distance_this=((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))**0.5
         if distance_this>maxxvalue:
             
             
             xx1, yy1, xx2, yy2 =secondmaxx[0]
             
             this_max=[]
             this_max.append(line)
             maxx=this_max
             maxxvalue=distance_this
             #print(maxx)
    linemax1x11,linemax1y11,linemax1x222,linemax1y22=maxx[0]
    secodmaxvalue=0
    #print(str(maxxvalue))
    for line in linexs:
         x1, y1, x2, y2 = line
         distance_this=((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))**0.5
         #print("distance_this:"+str(distance_this))
         if distance_this<maxxvalue :
            if distance_this>secodmaxvalue:
                if abs(y1-linemax1y11)>20:
                    #print("jin")
                    seconda_max=[]
                    #print(maxx)
                    seconda_max.append(line)
                    secondmaxx=seconda_max
                    secodmaxvalue=distance_this
    #print(secondmaxx)
    #print(maxx)
    maxy.append(lineys[0])
    for line in lineys:
         x1, y1, x2, y2 = line
         distance_y_this=((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))**0.5
         if distance_y_this>maxyvalue:
             
             this_y_max=[]
             this_y_max.append(line)
             maxy=this_y_max

    #####
    maxyyvalue=0
    mayy=[]
    secondmaxy=[]
    for line in lineys:
         x1, y1, x2, y2 = line
         distance_this=((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))**0.5
         if distance_this>maxyyvalue:
             
             
     
             
             this_max=[]
             this_max.append(line)
             mayy=this_max
             maxyyvalue=distance_this
             #print(maxx)
    ylinemax1x11,ylinemax1y11,ylinemax1x222,ylinemax1y22=mayy[0]
    secodmayyvalue=0
    #print(str(maxxvalue))
    for line in linexs:
         x1, y1, x2, y2 = line
         distance_this=((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))**0.5
         #print("distance_this:"+str(distance_this))
         if distance_this<maxyyvalue :
            if distance_this>secodmayyvalue:
                if abs(x1-ylinemax1x11)>20:
                    #print("jin")
                    seconda_max=[]
                    #print(maxx)
                    seconda_max.append(line)
                    secondmaxy=seconda_max
                    secodmayyvalue=distance_this
    #print(secondmaxx)
    #print(maxx)
    maxy.append(lineys[0])



    final_list=[]
    final_list.append(maxx[0])
    final_list.append(secondmaxx[0])
    final_list.append(maxy[0])
    linemax1x,linemax1y,linemax1x2,linemax1y2=maxx[0]
    linesecond1x,linesecond1y,linesecond1x2,linesecond1y2=secondmaxx[0]
    linemaxyx,linemaxyy,linemaxyx2,linemaxyy2=maxy[0]
    final_line_cu=[]
    distance_1=0
    if linemaxyx>(linemax1x+linemax1x2)/2:
         final_line_cu.append(maxx[0])
         final_line_cu.append(secondmaxx[0])
         #final_line_cu.append([linemax1x2,linemax1y2])
         #final_line_cu.append([linesecond1x2,linesecond1y2])
         distance_1=abs(linesecond1y2-linemax1y2)

    else:
        final_line_cu.append(maxx[0])
        final_line_cu.append(secondmaxx[0])
        #final_line_cu.append([linemax1x,linemax1y])
        #final_line_cu.append([linesecond1x,linesecond1y])
        distance_1=abs(linemax1y-linesecond1y)
    if(len(mayy)>0):
      final_line_cu.append(mayy[0])
    if(len(secondmaxy)>0):
       final_line_cu.append(secondmaxy[0])
    aa=distance_1/(heights)
    #print("hao:"+str(distance_1/(heights)))
    changdu=46-60*aa
    print("path:"+str(files))
    print("distance:"+str(changdu))
    
   
path = "test_image_result"
filelist2=os.listdir(path)
for files2 in filelist2:#遍历所有文件

     Olddir=os.path.join(path,files2);#原来的文件路径
       
     if os.path.isdir(Olddir):#如果是文件夹
      continue;
     src = cv.imread(Olddir)
     #cv.imshow("shufa", src)
     src = cv.imread(Olddir)
     #cv.imshow("shufa", src)
     line_detect_possible(src,files2)
     try:
        
        src = cv.imread(Olddir)
        #cv.imshow("shufa", src)
        #line_detect_possible(src,files2)
        #cv.waitKey(0)
        #cv.destroyAllWindows()
        #pass
     except Exception :
         pass

