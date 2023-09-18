import argparse

import matplotlib.pyplot as plt
import re



# Read from log file
def read_log(log_file):
    event_times = []
    with open(log_file, 'r') as f:
        lines = f.readlines()
        for line in lines:
            m = re.search(r'\[(\d+)\]', line)
            if m:
                event_times.append(int(m.group(1)))
    event_times.sort()
    return event_times


# print(event_times)

# calculate average throughput
def cal_avg_throughput(event_times, interval):
    begin_time = event_times[0]
    end_time = event_times[-1]
    print('begin_time: ' + str(begin_time))
    print('end_time: ' + str(end_time))
    points_num = (end_time - begin_time) // interval

    throughput = []
    for i in range(points_num):
        begin_slice = begin_time + i * interval
        end_slice = begin_time + (i + 1) * interval
        throughput.append(len([x for x in event_times if begin_slice <= x < end_slice]) / interval * 1000000000)
    return throughput


# Plot
def plot(throughput, interval):
    plt.plot(throughput)
    plt.xlabel('Time (s)')
    plt.ylabel('Throughput (events/s)')
    plt.title('Throughput')
    plt.show()


def plot2(throughput1, throughput2, interval, label1, label2):
    plt.plot(throughput1, label=label1)
    plt.plot(throughput2, label=label2)
    plt.xlabel('Time (s)')
    plt.ylabel('Throughput (events/s)')
    plt.title('Throughput')
    plt.legend()
    plt.show()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Plot throughput.')
    parser.add_argument('--mode', type=int, default=1, help='1: plot throughput; 2: plot 2 throughput')
    parser.add_argument('--log_file', type=str, default='sinkOperator1.log',
                        help='log file path')
    parser.add_argument('--log_file2', type=str, default='sinkOperator2.log',
                        help='log file path')
    parser.add_argument('--interval', type=int, default=1000000000, # 1s
                        help='interval (ns)')
    args = parser.parse_args()
    if args.mode == 1:
        event_times = read_log(args.log_file)
        throughput = cal_avg_throughput(event_times, args.interval)
        plot(throughput, args.interval)
    elif args.mode == 2:
        event_times1 = read_log(args.log_file)
        event_times2 = read_log(args.log_file2)
        throughput1 = cal_avg_throughput(event_times1, args.interval)
        throughput2 = cal_avg_throughput(event_times2, args.interval)
        plot2(throughput1, throughput2, args.interval, args.log_file, args.log_file2)
    else:
        print('Invalid mode.')
