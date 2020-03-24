'use strict';

const path                    = require('path');
const webpack                 = require('webpack');
const MiniCssExtractPlugin    = require("mini-css-extract-plugin");
const { CleanWebpackPlugin }  = require('clean-webpack-plugin');

module.exports = {
        resolve: {
            alias: {
                '../../theme.config$': path.join(__dirname, 'semanticui/theme.config')  
            }
        },
        entry: {
            site: __dirname + '/src/index.js'
        },
        output: {
            filename: 'semanticui-theme/js/[name].bundle.js',
            path: path.resolve(__dirname, 'dist')
        },
        optimization: {
            splitChunks: {
                   chunks: 'all'
                 }
        },
        module: {
            rules: [
                {
                    test: /\.less$/,
                    use: [
                      {
                        loader: MiniCssExtractPlugin.loader
                      },
                      'css-loader',
                      'less-loader'
                    ]
                },
                 // this rule handles images
                {
                    test: /\.jpe?g$|\.gif$|\.ico$|\.png$|\.svg$/,
                    use: 'file-loader?name=../resources/images/[name].[ext]?[hash]'
                },
                // the following 3 rules handle font extraction
                {
                    test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                    loader: 'file-loader?name=../resources/fonts/[name].[ext]&mimetype=application/font-woff'
                },
                {
                    test: /\.(ttf|eot)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                    loader: 'file-loader?name=../resources/fonts/[name].[ext]'
                },
                {
                test: /\.otf(\?.*)?$/,
                use: 'file-loader?name=../resources/fonts/[name].[ext]&mimetype=application/font-otf'
                }
            ]
        },
        plugins: [
            new CleanWebpackPlugin(),
            new webpack.NoEmitOnErrorsPlugin(),
            new MiniCssExtractPlugin({
                filename: 'semanticui-theme/css/[name].bundle.css'
            })
        ],
        stats: {
            assetsSort: "chunks",
            builtAt: true,
            children: false,
            chunkGroups: true,
            chunkOrigins: true,
            colors: false,
            errors: true,
            errorDetails: true,
            env: true,
            modules: false,
            performance: true,
            providedExports: false,
            source: false,
            warnings: true
        }
};
