using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace CalculatorGUI
{
    /// <summary>
    /// CoordnationCanvas.xaml 的交互逻辑
    /// </summary>
    public partial class CoordnationCanvas : UserControl
    {
        Point lookPoint = new Point(0, 0);
        public Point LookLocation
        {
            get { return lookPoint; }
            set
            {
                lookPoint = value;
                RebuildCoordnation();
            }
        }

        double step = 20;
        public double Step
        {
            get { return step; }
            set
            {
                step = value;
                RebuildCoordnation();
            }
        }

        double x_start = -100, x_end = 100, y_start = -100, y_end = 100;

        public double MinX
        {
            get { return x_start; }
            set
            {
                x_start = value;
                RebuildCoordnation();
            }
        }

        public double MinY
        {
            get { return y_start; }
            set
            {
                y_start = value;
                RebuildCoordnation();
            }
        }

        public double MaxX
        {
            get { return x_end; }
            set
            {
                x_end = value;
                RebuildCoordnation();
            }
        }

        public double MaxY
        {
            get { return y_end; }
            set
            {
                y_end = value;
                RebuildCoordnation();
            }
        }

        Brush drawBrush = new SolidColorBrush(Colors.GreenYellow);
        public Brush DrawBrush
        {
            get { return drawBrush; }
            set
            {
                drawBrush = value;
                RebuildCoordnation();
            }
        }

        Brush axisBrush = new SolidColorBrush(Colors.Red);
        public Brush AxisBrush
        {
            get { return axisBrush; }
            set
            {
                Brush axisBrush = value;
                RebuildCoordnation();
            }
        }

        Brush mouseAxisBrush = new SolidColorBrush(Colors.Blue);
        public Brush MouseAxisBrush
        {
            get { return mouseAxisBrush; }
            set
            {
                Brush mouseAxisBrush = value;
                RebuildCoordnation();
            }
        }

        public delegate void OnDrawDataFunc(object sender, PathGeometry drawProvider);

        public delegate double OnDymaticCalculateFunc(object sender, double xValue);

        public event OnDrawDataFunc OnDrawData;

        public event OnDymaticCalculateFunc OnDymaticCalculate;

        #region Internal Defindition

        bool ableDrawMouseAxis = false;

        static Point zeroPoint = new Point(0, 0);

        LineGeometry Mouse_X_Line = new LineGeometry(zeroPoint, zeroPoint), Mouse_Y_Line = new LineGeometry(zeroPoint, zeroPoint);

        PathGeometry mouseGeometry = new PathGeometry();

        Label displayer_X, displayer_Y;

        double canvas_width;
        double canvas_height;

        PathGeometry drawDataGeometry = new PathGeometry();

        #endregion

        public CoordnationCanvas()
        {
            InitializeComponent();

            //UpdateLayout();

        }

        Geometry GenCoordnationGeometry(double x_eye, double y_eye, double width, double height, double step)
        {
            PathGeometry geometry = new PathGeometry();

            Point baseOriginPoint = new Point(width / 2 - x_eye, height / 2 + y_eye);

            double y_base = baseOriginPoint.Y;
            y_base = y_base < 0 ? 0 : (y_base > height ? height : y_base);

            double x_base = baseOriginPoint.X;
            x_base = x_base < 0 ? 0 : (x_base > width ? width : x_base);

            {
                //绘制x轴
                geometry.AddGeometry(new LineGeometry(new Point(0, y_base), new Point(width, y_base)));

                for (double len = x_base; len < width; len = len + step)
                {
                    geometry.AddGeometry(new LineGeometry(new Point(len, y_base + 5), new Point(len, y_base - 5)));
                }

                for (double len = x_base - step; len >= 0; len = len - step)
                {
                    geometry.AddGeometry(new LineGeometry(new Point(len, y_base + 5), new Point(len, y_base - 5)));
                }
            }

            {
                //绘制y轴
                geometry.AddGeometry(new LineGeometry(new Point(x_base, 0), new Point(x_base, height)));

                for (double len = y_base; len < height; len = len + step)
                {
                    geometry.AddGeometry(new LineGeometry(new Point(x_base + 5, len), new Point(x_base - 5, len)));
                }

                for (double len = y_base - step; len >= 0; len = len - step)
                {
                    geometry.AddGeometry(new LineGeometry(new Point(x_base + 5, len), new Point(x_base - 5, len)));
                }
            }
            return geometry;
        }

        private void UserControl_Loaded(object sender, RoutedEventArgs e)
        {
            RebuildCoordnation();
        }

        void RebuildCoordnation()
        {
            canvas_height = this.ActualHeight;
            canvas_width = this.ActualWidth;

            Geometry geometry = GenCoordnationGeometry(lookPoint.X, lookPoint.Y, canvas_width, canvas_height, step);

            Path coordPath = new Path();
            coordPath.Data = geometry;
            coordPath.Stroke = AxisBrush;
            MyCanvas.Children.Add(coordPath);

            Path mousePath = new Path();
            mousePath.Data = mouseGeometry;
            mousePath.Stroke = MouseAxisBrush;
            MyCanvas.Children.Add(mousePath);

            displayer_X = new Label();
            displayer_X.Visibility = Visibility.Hidden;
            MyCanvas.Children.Add(displayer_X);

            displayer_Y = new Label();
            displayer_Y.Visibility = Visibility.Hidden;
            MyCanvas.Children.Add(displayer_Y);

            Path drawPath = new Path()
            {
                Stroke = DrawBrush,
                Data = drawDataGeometry
            };
            MyCanvas.Children.Add(drawPath);

            OnDrawData?.Invoke(this, drawDataGeometry);
        }

        private void MyCanvas_MouseEnter(object sender, MouseEventArgs e)
        {
            ableDrawMouseAxis = true;
            displayer_X.Visibility = Visibility.Visible;
            displayer_Y.Visibility = Visibility.Visible;
        }

        private void MyCanvas_MouseLeave(object sender, MouseEventArgs e)
        {
            ableDrawMouseAxis = false;
            mouseGeometry.Clear();
            displayer_X.Visibility = Visibility.Hidden;
            displayer_Y.Visibility = Visibility.Hidden;
        }

        public double GetRealCurrentX(double relative_value)
        {
            double normalizeValue = relative_value / canvas_width;
            double offsetValue = lookPoint.X / 2;
            //100 ----------- 200
            return x_start + (Math.Abs(x_end - x_start) * (relative_value / canvas_width)) + lookPoint.X / 2;
        }

        public double GetRealCurrentY(double relative_value)
        {
            return -y_start - (Math.Abs(y_end - y_start) * (relative_value / canvas_height)) - lookPoint.Y / 2;
        }

        public Point GetRealCurrentPoint(Point relative_value) => new Point(GetRealCurrentX(relative_value.X), GetRealCurrentY(relative_value.Y));

        public double GetRelativeCurrentX(double real_value)
        {
            return (real_value - lookPoint.X / 2 - x_start) / Math.Abs(x_end - x_start) * canvas_width;
        }

        public double GetRelativeCurrentY(double real_value)
        {
            return -(real_value - lookPoint.Y / 2 + y_start) / Math.Abs(y_end - y_start) * canvas_height;
        }

        public Point GetRelativeCurrentPoint(Point real_point) => new Point(GetRelativeCurrentX(real_point.X), GetRelativeCurrentY(real_point.Y));

        private void MyCanvas_MouseMove(object sender, MouseEventArgs e)
        {
            if (ableDrawMouseAxis)
            {
                Point current_p = e.GetPosition(MyCanvas);

                mouseGeometry.Clear();

                Mouse_X_Line.StartPoint = new Point(0, current_p.Y);
                Mouse_X_Line.EndPoint = new Point(canvas_width, current_p.Y);
                displayer_X.Content = $"{GetRealCurrentY(current_p.Y):F3}";

                Mouse_Y_Line.StartPoint = new Point(current_p.X, 0);
                Mouse_Y_Line.EndPoint = new Point(current_p.X, canvas_height);
                displayer_Y.Content = $"{GetRealCurrentX(current_p.X):F3}";

                MyCanvas.UpdateLayout();

                mouseGeometry.AddGeometry(Mouse_X_Line);
                mouseGeometry.AddGeometry(Mouse_Y_Line);

                if (current_p.X > canvas_width / 2)
                {
                    Canvas.SetLeft(displayer_X, 0);
                    Canvas.SetLeft(displayer_Y, current_p.X - displayer_Y.ActualWidth);
                }
                else
                {
                    Canvas.SetLeft(displayer_X, canvas_width - displayer_X.ActualWidth);
                    Canvas.SetLeft(displayer_Y, current_p.X);
                }

                if (current_p.Y > canvas_height / 2)
                {
                    Canvas.SetTop(displayer_X, current_p.Y - displayer_X.ActualHeight);
                    Canvas.SetTop(displayer_Y, 0);
                }
                else
                {
                    Canvas.SetTop(displayer_X, current_p.Y);
                    Canvas.SetTop(displayer_Y, canvas_height - displayer_Y.ActualWidth);
                }
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="lines"></param>
        /// <param name="isFixedCoord">false=相对坐标.true=绝对坐标，</param>
        public void AppendLines(Point[] lines, bool isFixedCoord = true)
        {
            if (isFixedCoord)
            {
                for (int i = 0; i < lines.Length; i++)
                {
                    lines[i] = GetRelativeCurrentPoint(lines[i]);
                }
            }

            for (int i = 1; i < lines.Length; i++)
            {
                drawDataGeometry.AddGeometry(new LineGeometry((lines[i - 1]), (lines[i])));
            }
        }

        public void ClearAllLines()
        {
            drawDataGeometry.Clear();
        }
    }
}
